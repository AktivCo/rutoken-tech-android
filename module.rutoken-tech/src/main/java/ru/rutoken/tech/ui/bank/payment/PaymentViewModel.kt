/*
 * Copyright (c) 2024, Aktiv-Soft JSC.
 * See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.tech.ui.bank.payment

import android.content.Context
import androidx.annotation.MainThread
import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.rutoken.pkcs11wrapper.main.Pkcs11Session
import ru.rutoken.pkcs11wrapper.rutoken.main.RtPkcs11Session
import ru.rutoken.tech.R
import ru.rutoken.tech.session.AppSessionHolder
import ru.rutoken.tech.session.BankUserLoginAppSession
import ru.rutoken.tech.session.requireBankUserLoginSession
import ru.rutoken.tech.tokenmanager.TokenManager
import ru.rutoken.tech.ui.bank.payments.Payment
import ru.rutoken.tech.ui.bank.payments.UserActionType
import ru.rutoken.tech.ui.components.AppIcons
import ru.rutoken.tech.ui.tokenconnector.TokenConnector
import ru.rutoken.tech.ui.utils.DialogData
import ru.rutoken.tech.ui.utils.DialogDataWithIcon
import ru.rutoken.tech.ui.utils.DialogState
import ru.rutoken.tech.ui.utils.callPkcs11Operation
import ru.rutoken.tech.ui.utils.toErrorDialogData
import ru.rutoken.tech.usecase.CmsOperationProvider
import ru.rutoken.tech.utils.VerifyCmsResult
import ru.rutoken.tech.utils.logd
import ru.rutoken.tech.utils.loge
import java.io.File
import kotlin.coroutines.cancellation.CancellationException

private val verifyValidSignatureDialogData =
    DialogDataWithIcon({ AppIcons.ValidSignature() }, R.string.verify_valid_signature, null)
private val verifyInvalidChainDialogData =
    DialogDataWithIcon({ AppIcons.ValidSignature() }, R.string.verify_valid_signature, R.string.verify_invalid_chain)
private val verifyInvalidSignatureDialogData =
    DialogDataWithIcon({ AppIcons.InvalidSignature() }, R.string.verify_invalid_signature, null)

class PaymentViewModel(
    private val applicationContext: Context,
    private val sessionHolder: AppSessionHolder,
    private val tokenManager: TokenManager,
    private val paymentTitle: String
) : ViewModel() {
    val tokenConnector = TokenConnector()

    // BankUserLoginAppSession instance MUST exist by the time this ViewModel is instantiated
    private val bankUserLoginSession: BankUserLoginAppSession
        get() = sessionHolder.requireBankUserLoginSession()

    private val _payment = MutableLiveData(bankUserLoginSession.payments.first { it.title == paymentTitle })
    val payment: LiveData<Payment> get() = _payment

    private val _operationCompleted = MutableLiveData(false)
    val operationCompleted: LiveData<Boolean> get() = _operationCompleted

    private val _sharedFiles = MutableLiveData<List<File>>()
    val sharedFiles: LiveData<List<File>> = _sharedFiles

    private val _navigateToTokenAuth = MutableLiveData<Boolean>()
    val navigateToTokenAuth: LiveData<Boolean> = _navigateToTokenAuth

    private val _showProgress = MutableLiveData<Boolean>()
    val showProgress: LiveData<Boolean> = _showProgress

    private val _operationCompletedDialogState = MutableLiveData<DialogState>()
    val operationCompletedDialogState: LiveData<DialogState> get() = _operationCompletedDialogState

    private val _errorDialogState = MutableLiveData<DialogState>()
    val errorDialogState: LiveData<DialogState> = _errorDialogState

    @MainThread
    fun onUserActionButtonClicked() {
        with(_payment.value!!) {
            when (userActionType) {
                UserActionType.SIGN -> {
                    bankUserLoginSession.operationWithToken = ::signPaymentOnTokenAuth
                    _navigateToTokenAuth.value = true
                }

                UserActionType.VERIFY -> {
                    verifyPaymentSignature()
                }

                UserActionType.ENCRYPT -> {
                    encryptPayment()
                }

                UserActionType.DECRYPT -> {
                    bankUserLoginSession.operationWithToken = ::decryptPaymentOnTokenAuth
                    _navigateToTokenAuth.value = true
                }
            }
        }
    }

    @MainThread
    fun onDismissOperationCompletedDialog() {
        _operationCompletedDialogState.value = DialogState(showDialog = false)
    }

    @MainThread
    fun onSharePaymentClicked() {
        viewModelScope.launch(Dispatchers.IO) {
            _sharedFiles.postValue(_payment.value!!.getSharedData(applicationContext))
        }
    }

    @MainThread
    fun resetOnSharePaymentClicked() {
        _sharedFiles.value = emptyList()
    }

    @MainThread
    fun dismissErrorDialog() {
        _errorDialogState.value = DialogState(showDialog = false)
    }

    @MainThread
    fun resetNavigateToTokenAuthEvent() {
        _navigateToTokenAuth.value = false
    }

    @WorkerThread
    private fun signPaymentOnTokenAuth(session: RtPkcs11Session) {
        session.signPayment(_payment.value!!, bankUserLoginSession.certificateCkaId, applicationContext)
        _operationCompleted.postValue(true)
        _operationCompletedDialogState.postValue(DialogState(true, DialogData(R.string.sign_operation_completed)))
    }

    private fun verifyPaymentSignature(provider: CmsOperationProvider = CmsOperationProvider.PKCS11_WRAPPER) {
        viewModelScope.launch(Dispatchers.IO) {
            when (provider) {
                CmsOperationProvider.PKCS11_WRAPPER -> verifyPaymentSignatureViaPkcs11Wrapper()
                CmsOperationProvider.BOUNCY_CASTLE -> verifyPaymentSignatureViaBouncyCastle()
            }
        }
    }

    private suspend fun verifyPaymentSignatureViaPkcs11Wrapper() {
        try {
            val tokenSerial = bankUserLoginSession.tokenSerial
            val token = tokenConnector.findTokenBySerialNumber(tokenManager, tokenSerial).token

            callPkcs11Operation(_showProgress, tokenManager, tokenSerial) {
                token.openSession(true).use { session ->
                    doVerifyPaymentSignature(CmsOperationProvider.PKCS11_WRAPPER, session)
                }
            }
        } catch (e: CancellationException) {
            logd(e) { "Connect token dialog was dismissed" }
        } catch (e: Exception) {
            loge(e) { "Verifying payment via pkcs11wrapper failed" }
            _errorDialogState.postValue(DialogState(showDialog = true, data = e.toErrorDialogData()))
        }
    }

    private fun verifyPaymentSignatureViaBouncyCastle() {
        try {
            _showProgress.postValue(true)
            doVerifyPaymentSignature(CmsOperationProvider.BOUNCY_CASTLE)
        } catch (e: Exception) {
            loge(e) { "Verifying payment via Bouncy Castle failed" }
            _errorDialogState.postValue(DialogState(showDialog = true, data = e.toErrorDialogData()))
        } finally {
            _showProgress.postValue(false)
        }
    }

    private fun doVerifyPaymentSignature(provider: CmsOperationProvider, session: RtPkcs11Session? = null) {
        val verifyResult = verifyPaymentSignature(_payment.value!!, applicationContext, provider, session)
        _operationCompleted.postValue(true)

        when (verifyResult) {
            VerifyCmsResult.SUCCESS ->
                _operationCompletedDialogState.postValue(DialogState(true, verifyValidSignatureDialogData))

            VerifyCmsResult.CERTIFICATE_CHAIN_NOT_VERIFIED ->
                _operationCompletedDialogState.postValue(DialogState(true, verifyInvalidChainDialogData))

            VerifyCmsResult.SIGNATURE_INVALID ->
                _operationCompletedDialogState.postValue(DialogState(true, verifyInvalidSignatureDialogData))
        }
    }

    private fun encryptPayment() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _showProgress.postValue(true)
                encryptPayment(_payment.value!!, applicationContext)
                _operationCompleted.postValue(true)
                _operationCompletedDialogState.postValue(
                    DialogState(true, DialogData(R.string.encrypt_operation_completed))
                )
            } catch (e: Exception) {
                loge(e) { "Payment encryption failed" }
                _errorDialogState.postValue(DialogState(showDialog = true, data = e.toErrorDialogData()))
            } finally {
                _showProgress.postValue(false)
            }
        }
    }

    @WorkerThread
    private fun decryptPaymentOnTokenAuth(session: Pkcs11Session) {
        session.decryptPayment(
            _payment.value!!,
            bankUserLoginSession.certificateCkaId,
            bankUserLoginSession.certificate,
            applicationContext
        )
        _operationCompleted.postValue(true)
        _operationCompletedDialogState.postValue(DialogState(true, DialogData(R.string.decrypt_operation_completed)))
    }
}