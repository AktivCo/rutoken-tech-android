/*
 * Copyright (c) 2024, Aktiv-Soft JSC.
 * See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.tech.ui.ca.generateobjects.certificate

import androidx.annotation.MainThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.rutoken.pkcs11wrapper.constant.standard.Pkcs11UserType.CKU_USER
import ru.rutoken.pkcs11wrapper.`object`.certificate.Pkcs11CertificateObject
import ru.rutoken.pkcs11wrapper.rutoken.main.RtPkcs11Token
import ru.rutoken.tech.R
import ru.rutoken.tech.pkcs11.createobjects.createGostCertificate
import ru.rutoken.tech.pkcs11.findobjects.findGost256KeyPairByCkaId
import ru.rutoken.tech.session.CkaIdString
import ru.rutoken.tech.session.RutokenTechSessionHolder
import ru.rutoken.tech.session.requireCaSession
import ru.rutoken.tech.tokenmanager.TokenManager
import ru.rutoken.tech.ui.tokenconnector.TokenConnector
import ru.rutoken.tech.ui.utils.DialogData
import ru.rutoken.tech.ui.utils.DialogState
import ru.rutoken.tech.ui.utils.callPkcs11Operation
import ru.rutoken.tech.ui.utils.toErrorDialogData
import ru.rutoken.tech.utils.BusinessRuleCase
import ru.rutoken.tech.utils.BusinessRuleException
import ru.rutoken.tech.utils.logd
import ru.rutoken.tech.utils.loge

class GenerateCertificateViewModel(
    private val tokenManager: TokenManager,
    private val sessionHolder: RutokenTechSessionHolder
) : ViewModel() {
    val tokenConnector = TokenConnector()

    private val _showProgress = MutableLiveData<Boolean>()
    val showProgress: LiveData<Boolean> = _showProgress

    private val _successDialogState = MutableLiveData<DialogState>()
    val successDialogState: LiveData<DialogState> = _successDialogState

    private val _errorDialogState = MutableLiveData<DialogState>()
    val errorDialogState: LiveData<DialogState> = _errorDialogState

    // CaRutokenTechSession MUST exist and keyPairs MUST NOT be empty by the time this ViewModel is instantiated
    private val _keyPairs =
        MutableLiveData<List<CkaIdString>>(sessionHolder.requireCaSession().keyPairs)
    val keyPairs: LiveData<List<CkaIdString>> get() = _keyPairs

    private val _shouldLogout = MutableLiveData(false)
    val shouldLogout: LiveData<Boolean> get() = _shouldLogout

    private var hasPinChanged = false

    fun generateGostCertificate(keyPairCkaId: CkaIdString, owner: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val caSession = sessionHolder.requireCaSession()
                val tokenSerial = caSession.tokenSerial
                val token = tokenConnector.findTokenBySerialNumber(tokenManager, tokenSerial) as RtPkcs11Token

                callPkcs11Operation(_showProgress, tokenManager, tokenSerial) {
                    createGostCertificate(
                        token,
                        caSession.tokenUserPin,
                        keyPairCkaId,
                        createDN(owner),
                        null,
                        EXTENSIONS
                    )
                    caSession.keyPairs.remove(keyPairCkaId)
                    _successDialogState.postValue(
                        DialogState(
                            showDialog = true,
                            data = DialogData(text = R.string.test_certificate_generated)
                        )
                    )
                }
            } catch (e: CancellationException) {
                logd(e) { "Connect token dialog was dismissed" }
            } catch (e: Exception) {
                loge<GenerateCertificateViewModel>(e) { "Certificate generation failed" }
                if (e is BusinessRuleException && e.case is BusinessRuleCase.IncorrectPin)
                    hasPinChanged = true

                _errorDialogState.postValue(DialogState(showDialog = true, data = e.toErrorDialogData()))
            }
        }
    }

    @MainThread
    fun dismissSuccessDialog() {
        _successDialogState.value = DialogState(showDialog = false)
    }

    @MainThread
    fun dismissErrorDialog() {
        _errorDialogState.value = DialogState(showDialog = false)
        if (hasPinChanged) _shouldLogout.postValue(true)
    }

    private fun createGostCertificate(
        token: RtPkcs11Token,
        userPin: String,
        keyPairCkaId: CkaIdString,
        dn: List<String>,
        attributes: List<String>?,
        extensions: List<String>
    ): Pkcs11CertificateObject {
        return token.openSession(true).use { session ->
            session.login(CKU_USER, userPin).use {
                val keyPair = try {
                    session.findGost256KeyPairByCkaId(keyPairCkaId.toByteArray())
                } catch (_: IllegalStateException) {
                    throw BusinessRuleException(BusinessRuleCase.NoSuchKeyPair)
                }
                session.createGostCertificate(keyPair, dn, attributes, extensions)
            }
        }
    }
}
