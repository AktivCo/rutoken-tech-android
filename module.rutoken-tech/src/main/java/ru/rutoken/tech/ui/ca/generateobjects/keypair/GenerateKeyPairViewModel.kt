/*
 * Copyright (c) 2024, Aktiv-Soft JSC.
 * See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.tech.ui.ca.generateobjects.keypair

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.rutoken.pkcs11wrapper.constant.standard.Pkcs11UserType.CKU_USER
import ru.rutoken.pkcs11wrapper.datatype.Pkcs11Date
import ru.rutoken.pkcs11wrapper.main.Pkcs11Token
import ru.rutoken.tech.R
import ru.rutoken.tech.pkcs11.createobjects.EcKeyPairParams
import ru.rutoken.tech.pkcs11.createobjects.GostKeyPairParams
import ru.rutoken.tech.pkcs11.createobjects.createEcKeyPair
import ru.rutoken.tech.pkcs11.createobjects.createGostKeyPair
import ru.rutoken.tech.session.AppSessionHolder
import ru.rutoken.tech.session.requireCaSession
import ru.rutoken.tech.tokenmanager.TokenManager
import ru.rutoken.tech.ui.utils.DialogData
import ru.rutoken.tech.ui.utils.DialogState
import ru.rutoken.tech.ui.utils.callPkcs11Operation
import ru.rutoken.tech.ui.utils.toErrorDialogData
import ru.rutoken.tech.ui.vmdelegate.ConnectTokenDelegate
import ru.rutoken.tech.utils.BusinessRuleCase
import ru.rutoken.tech.utils.BusinessRuleException
import ru.rutoken.tech.utils.logd
import ru.rutoken.tech.utils.loge
import java.time.Period
import java.time.ZonedDateTime

class GenerateKeyPairViewModel(
    private val tokenManager: TokenManager,
    private val sessionHolder: AppSessionHolder,
) : ViewModel() {
    val connectTokenDelegate = ConnectTokenDelegate(viewModelScope)

    private val _showProgress = MutableLiveData<Boolean>()
    val showProgress: LiveData<Boolean> get() = _showProgress

    private val _successDialogState = MutableLiveData<DialogState>()
    val successDialogState: LiveData<DialogState> get() = _successDialogState

    private val _errorDialogState = MutableLiveData<DialogState>()
    val errorDialogState: LiveData<DialogState> get() = _errorDialogState

    private val _keyPairId = MutableLiveData(CkaID())
    val keyPairId: LiveData<CkaID>
        get() = _keyPairId

    private val _shouldLogout = MutableLiveData(false)
    val shouldLogout: LiveData<Boolean> get() = _shouldLogout

    private val _algorithm = MutableLiveData(KeyPairAlgorithm.GOST_2012_256)
    val algorithm: LiveData<KeyPairAlgorithm> get() = _algorithm
    private var hasPinChanged = false

    fun selectAlgorithm(algorithm: KeyPairAlgorithm) {
        _algorithm.value = algorithm
    }
    fun generateKeyPair() {
        val algorithm = _algorithm.value!!
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val keyPairValidityNotBefore = ZonedDateTime.now()
                    val keyPairValidityNotAfter = keyPairValidityNotBefore + Period.ofYears(3)

                    val caSession = sessionHolder.requireCaSession()
                    val tokenSerial = caSession.tokenSerial
                    val token = connectTokenDelegate.findTokenBySerialNumber(tokenManager, tokenSerial).token

                    callPkcs11Operation(_showProgress, tokenManager, tokenSerial) {
                        createKeyPair(
                            token,
                            caSession.tokenUserPin,
                            _keyPairId.value!!.rawValue,
                            algorithm,
                            keyPairValidityNotBefore,
                            keyPairValidityNotAfter
                        )
                        caSession.keyPairs.add(0, _keyPairId.value!!)

                        _successDialogState.postValue(
                            DialogState(
                                showDialog = true,
                                data = DialogData(text = R.string.key_pair_generated)
                            )
                        )
                    }
                } catch (e: CancellationException) {
                    logd(e) { "Connect token dialog was dismissed" }
                } catch (e: Exception) {
                    loge<GenerateKeyPairViewModel>(e) { "Key pair generation failed" }
                    if (e is BusinessRuleException && e.case is BusinessRuleCase.IncorrectPin)
                        hasPinChanged = true

                    _errorDialogState.postValue(DialogState(showDialog = true, data = e.toErrorDialogData()))
                }
            }
        }
    }

    fun dismissSuccessDialog() {
        _successDialogState.value = DialogState(showDialog = false)
    }

    fun dismissErrorDialog() {
        _errorDialogState.value = DialogState(showDialog = false)
        if (hasPinChanged) _shouldLogout.postValue(true)
    }

    private suspend fun createKeyPair(
        token: Pkcs11Token,
        userPin: String,
        ckaId: ByteArray,
        algorithm: KeyPairAlgorithm,
        keyPairValidityNotBefore: ZonedDateTime,
        keyPairValidityNotAfter: ZonedDateTime,
    ) {
        val notBefore = Pkcs11Date(keyPairValidityNotBefore.toLocalDate())
        val notAfter = Pkcs11Date(keyPairValidityNotAfter.toLocalDate())

        token.openSession(true).use { session ->
            session.login(CKU_USER, userPin).use {
                when (algorithm) {
                    KeyPairAlgorithm.GOST_2012_256 ->
                        session.createGostKeyPair(GostKeyPairParams.GOST_2012_256, ckaId, notBefore, notAfter)
                    KeyPairAlgorithm.ECDSA_SECP256R1 ->
                        session.createEcKeyPair(EcKeyPairParams.SECP256R1, ckaId, notBefore, notAfter)
                }
            }
        }
    }
}
