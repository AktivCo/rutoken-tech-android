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
import ru.rutoken.tech.pkcs11.createobjects.createCertificate
import ru.rutoken.tech.pkcs11.createobjects.createEcCertificate
import ru.rutoken.tech.pkcs11.findobjects.findEcKeyPairByCkaId
import ru.rutoken.tech.pkcs11.findobjects.findGost256KeyPairByCkaId
import ru.rutoken.tech.session.AppSessionHolder
import ru.rutoken.tech.session.requireCaSession
import ru.rutoken.tech.tokenmanager.TokenManager
import ru.rutoken.tech.ui.ca.generateobjects.keypair.CkaID
import ru.rutoken.tech.ui.utils.DialogData
import ru.rutoken.tech.ui.utils.DialogState
import ru.rutoken.tech.ui.utils.callPkcs11Operation
import ru.rutoken.tech.ui.utils.toErrorDialogData
import ru.rutoken.tech.ui.vmdelegate.ConnectTokenDelegate
import ru.rutoken.tech.utils.BusinessRuleCase
import ru.rutoken.tech.utils.BusinessRuleException
import ru.rutoken.tech.utils.logd
import ru.rutoken.tech.utils.loge

class GenerateCertificateViewModel(
    private val tokenManager: TokenManager,
    private val sessionHolder: AppSessionHolder,
) : ViewModel() {
    val connectTokenDelegate = ConnectTokenDelegate(viewModelScope)

    private val _showProgress = MutableLiveData<Boolean>()
    val showProgress: LiveData<Boolean> = _showProgress

    private val _successDialogState = MutableLiveData<DialogState>()
    val successDialogState: LiveData<DialogState> = _successDialogState

    private val _errorDialogState = MutableLiveData<DialogState>()
    val errorDialogState: LiveData<DialogState> = _errorDialogState

    // CaAppSession MUST exist and keyPairs MUST NOT be empty by the time this ViewModel is instantiated
    private val _keyPairs = MutableLiveData<List<CkaID>>(sessionHolder.requireCaSession().keyPairs)
    val keyPairs: LiveData<List<CkaID>>
        get() = _keyPairs

    private val _shouldLogout = MutableLiveData(false)
    val shouldLogout: LiveData<Boolean> get() = _shouldLogout

    private var hasPinChanged = false

    fun generateCertificate(keyPairCkaId: CkaID, owner: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val caSession = sessionHolder.requireCaSession()
                val tokenSerial = caSession.tokenSerial
                val token = connectTokenDelegate.findTokenBySerialNumber(tokenManager, tokenSerial).token

                callPkcs11Operation(_showProgress, tokenManager, tokenSerial) {
                    createCertificate(
                        token,
                        caSession.tokenUserPin,
                        keyPairCkaId.rawValue,
                        createDN(owner),
                        null,
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

    private suspend fun createCertificate(
        token: RtPkcs11Token,
        userPin: String,
        keyPairCkaId: ByteArray,
        dn: List<String>,
        attributes: List<String>?,
    ) : Pkcs11CertificateObject {
        return token.openSession(true).use { session ->
            session.login(CKU_USER, userPin).use {
                val gostKeyPair = findOrNull { session.findGost256KeyPairByCkaId(keyPairCkaId) }
                if (gostKeyPair != null)
                    return@use session.createCertificate(gostKeyPair, dn, attributes, EXTENSIONS)

                val ecKeyPair = findOrNull { session.findEcKeyPairByCkaId(keyPairCkaId) }
                if (ecKeyPair != null)
                    return@use session.createEcCertificate(ecKeyPair, dn)

                throw BusinessRuleException(BusinessRuleCase.NoSuchKeyPair)
            }
        }
    }

    private inline fun <T> findOrNull(block: () -> T): T? = try {
        block()
    } catch (_: IllegalStateException) {
        null
    }
}
