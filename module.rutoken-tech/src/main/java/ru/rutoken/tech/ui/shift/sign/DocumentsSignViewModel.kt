/*
 * Copyright (c) 2025, Aktiv-Soft JSC.
 * See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.tech.ui.shift.sign

import android.content.Context
import androidx.annotation.MainThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.bouncycastle.cert.X509CertificateHolder
import ru.rutoken.pkcs11wrapper.constant.standard.Pkcs11UserType
import ru.rutoken.pkcs11wrapper.datatype.Pkcs11TokenInfo
import ru.rutoken.pkcs11wrapper.`object`.key.Pkcs11GostPrivateKeyObject
import ru.rutoken.pkcs11wrapper.rutoken.main.RtPkcs11Session
import ru.rutoken.pkcs11wrapper.rutoken.main.RtPkcs11Token
import ru.rutoken.tech.R
import ru.rutoken.tech.ca.LocalCA
import ru.rutoken.tech.helpers.AssetsHelper
import ru.rutoken.tech.pkcs11.findobjects.findGost256CertificateAndKeyContainers
import ru.rutoken.tech.pkcs11.serialNumberTrimmed
import ru.rutoken.tech.repository.shift.signeddocument.ShiftSignedDocumentRepository
import ru.rutoken.tech.session.AppSessionHolder
import ru.rutoken.tech.session.DocumentsPreviewInfo
import ru.rutoken.tech.session.ShiftUserLoginAppSession
import ru.rutoken.tech.session.requireShiftUserLoginSession
import ru.rutoken.tech.tokenmanager.TokenManager
import ru.rutoken.tech.ui.shift.documents.Document
import ru.rutoken.tech.ui.shift.documents.SignedDocumentsGroup
import ru.rutoken.tech.ui.tokenconnector.TokenConnector
import ru.rutoken.tech.ui.utils.DialogState
import ru.rutoken.tech.ui.utils.ErrorDialogData
import ru.rutoken.tech.ui.utils.callPkcs11Operation
import ru.rutoken.tech.ui.utils.toErrorDialogData
import ru.rutoken.tech.usecase.CmsOperations
import ru.rutoken.tech.utils.BusinessRuleCase.IncorrectPin
import ru.rutoken.tech.utils.BusinessRuleCase.PinLocked
import ru.rutoken.tech.utils.BusinessRuleException
import ru.rutoken.tech.utils.getFullName
import ru.rutoken.tech.utils.logd
import ru.rutoken.tech.utils.loge
import java.time.LocalDate
import kotlin.coroutines.cancellation.CancellationException

class DocumentsSignViewModel(
    private val shiftSignedDocumentRepository: ShiftSignedDocumentRepository,
    private val tokenManager: TokenManager,
    private val sessionHolder: AppSessionHolder,
    private val applicationContext: Context,
    private val assetsHelper: AssetsHelper,
) : ViewModel() {
    val tokenConnector = TokenConnector(viewModelScope)

    private val shiftUserLoginSession: ShiftUserLoginAppSession
        get() = sessionHolder.requireShiftUserLoginSession()

    private val _showEnterPinBottomSheet = MutableLiveData(true)
    val showEnterPinBottomSheet: LiveData<Boolean> get() = _showEnterPinBottomSheet

    private val _showProgress = MutableLiveData<Boolean>()
    val showProgress: LiveData<Boolean> get() = _showProgress

    private val _errorDialogState = MutableLiveData<DialogState>()
    val errorDialogState: LiveData<DialogState> get() = _errorDialogState

    private val _signatoriesInfo = MutableLiveData<List<String>>(emptyList())
    val signatoriesInfo: LiveData<List<String>> get() = _signatoriesInfo

    private val _navigateBack = MutableLiveData(false)
    val navigateBack: LiveData<Boolean> get() = _navigateBack

    private var documentsToSign = shiftUserLoginSession.chosenDocuments.documents

    @MainThread
    fun onFinishSigningClick() {
        viewModelScope.launch {
            val newSignedDocuments = SignedDocumentsGroup(
                documents = documentsToSign,
                signatories = _signatoriesInfo.value!!,
                date = LocalDate.now()
            )

            withContext(Dispatchers.IO) {
                shiftSignedDocumentRepository.addSignedDocuments(
                    documentsGroup = newSignedDocuments,
                    sessionId = shiftUserLoginSession.userId
                )
            }

            shiftUserLoginSession.signedDocuments =
                shiftUserLoginSession.signedDocuments.toMutableList().apply { add(newSignedDocuments) }

            shiftUserLoginSession.documents =
                shiftUserLoginSession.documents.filter { document ->
                    !newSignedDocuments.documents.any { signedDocument -> document.title == signedDocument.title }
                }

            with(shiftUserLoginSession.chosenDocuments) {
                shiftUserLoginSession.chosenDocuments = DocumentsPreviewInfo(documentsToSign, startDocument)
            }

            _navigateBack.value = true
        }
    }

    @MainThread
    fun dismissErrorDialog() {
        _errorDialogState.value = DialogState(showDialog = false)
    }

    @MainThread
    fun onSignClicked() {
        _showEnterPinBottomSheet.value = true
    }

    fun onPinCodeEntered(tokenUserPin: String, invalidPinBlock: (String) -> Unit) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val token = tokenConnector.findFirstToken(tokenManager).token

                    withTokenSession(token, token.tokenInfo, tokenUserPin) { session ->
                        val tokenContainers = session.findGost256CertificateAndKeyContainers()
                        val firstContainer = tokenContainers.getOrNull(0)
                        if (firstContainer != null) {
                            documentsToSign = session.signDocuments(
                                documents = documentsToSign,
                                certificate = firstContainer.certificate,
                                privateKey = firstContainer.keyPair.privateKey,
                            )

                            _signatoriesInfo.postValue(
                                _signatoriesInfo.value!!.toMutableList().apply {
                                    add(firstContainer.certificate.getFullName())
                                }
                            )
                        } else {
                            _errorDialogState.postValue(
                                DialogState(
                                    showDialog = true,
                                    data = ErrorDialogData(
                                        title = R.string.rutoken_has_no_certificates,
                                        text = R.string.use_ca_to_create_certificate
                                    )
                                )
                            )
                        }
                    }
                    onClosePincodeBottomSheet()
                } catch (e: CancellationException) {
                    logd(e) { "Connect token dialog was dismissed" }
                } catch (e: Exception) {
                    loge<DocumentsSignViewModel>(e) { "Add user to signatory failed" }
                    handleTokenError(e, invalidPinBlock)
                }
            }
        }
    }

    fun onClosePincodeBottomSheet() {
        _showEnterPinBottomSheet.postValue(false)
        if (_signatoriesInfo.value!!.isEmpty()) _navigateBack.postValue(true)
    }

    private fun handleTokenError(exception: Throwable, invalidPinBlock: (String) -> Unit) {
        val defaultErrorHandle = {
            _errorDialogState.postValue(DialogState(showDialog = true, data = exception.toErrorDialogData()))
        }

        if (exception is BusinessRuleException) {
            when (exception.case) {
                is IncorrectPin ->
                    invalidPinBlock(
                        applicationContext.getString(R.string.invalid_pin_supporting, exception.case.retryLeft)
                    )

                is PinLocked -> {
                    invalidPinBlock(
                        applicationContext.getString(R.string.invalid_pin_supporting, 0)
                    )
                    defaultErrorHandle()
                }

                else -> defaultErrorHandle()
            }
        } else {
            defaultErrorHandle()
        }
    }

    private suspend fun withTokenSession(
        token: RtPkcs11Token,
        tokenInfo: Pkcs11TokenInfo,
        tokenUserPin: String,
        block: suspend (RtPkcs11Session) -> Unit
    ) {
        callPkcs11Operation(_showProgress, tokenManager, tokenInfo.serialNumberTrimmed) {
            token.openSession(false).use { session ->
                session.login(Pkcs11UserType.CKU_USER, tokenUserPin).use {
                    block(session)
                }
            }
        }
    }

    private suspend fun RtPkcs11Session.signDocuments(
        documents: List<Document>,
        certificate: X509CertificateHolder,
        privateKey: Pkcs11GostPrivateKeyObject,
    ): List<Document> {
        return documents.map { document ->
            document.copy(
                signedCms = CmsOperations.signDetachedGost256Hardware(
                    session = this@signDocuments,
                    data = assetsHelper.loadAsset(document.assetName),
                    existingCmsBytes = document.signedCms,
                    privateKey = privateKey,
                    certificate = certificate,
                    additionalCertificates = listOf(X509CertificateHolder(LocalCA.caCertificate))
                )
            )
        }
    }
}
