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
import ru.rutoken.tech.pkcs11.createobjects.GostKeyPair
import ru.rutoken.tech.pkcs11.createobjects.GostKeyPairParams
import ru.rutoken.tech.pkcs11.createobjects.createGostKeyPair
import ru.rutoken.tech.pkcs11.createobjects.generateCkaId
import ru.rutoken.tech.session.AppSessionHolder
import ru.rutoken.tech.session.CkaIdString
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
import java.time.Period
import java.time.ZonedDateTime

class GenerateKeyPairViewModel(
    private val tokenManager: TokenManager,
    private val sessionHolder: AppSessionHolder
) : ViewModel() {
    val tokenConnector = TokenConnector()

    private val _showProgress = MutableLiveData<Boolean>()
    val showProgress: LiveData<Boolean> get() = _showProgress

    private val _successDialogState = MutableLiveData<DialogState>()
    val successDialogState: LiveData<DialogState> get() = _successDialogState

    private val _errorDialogState = MutableLiveData<DialogState>()
    val errorDialogState: LiveData<DialogState> get() = _errorDialogState

    private val _keyPairId = MutableLiveData("")
    val keyPairId: LiveData<CkaIdString> get() = _keyPairId

    private val _shouldLogout = MutableLiveData(false)
    val shouldLogout: LiveData<Boolean> get() = _shouldLogout

    private var hasPinChanged = false

    fun generateKeyPairId() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _keyPairId.postValue(generateCkaId().toString(Charsets.UTF_8))
            }
        }
    }

    fun generateGostKeyPair(ckaId: CkaIdString, keyPairParams: GostKeyPairParams) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val keyPairValidityNotBefore = ZonedDateTime.now()
                    val keyPairValidityNotAfter = keyPairValidityNotBefore + Period.ofYears(3)

                    val caSession = sessionHolder.requireCaSession()
                    val tokenSerial = caSession.tokenSerial
                    val token = tokenConnector.findTokenBySerialNumber(tokenManager, tokenSerial).token

                    callPkcs11Operation(_showProgress, tokenManager, tokenSerial) {
                        createGostKeyPair(
                            token,
                            caSession.tokenUserPin,
                            ckaId, keyPairParams,
                            keyPairValidityNotBefore,
                            keyPairValidityNotAfter
                        )
                        caSession.keyPairs.add(0, ckaId)
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

    private fun createGostKeyPair(
        token: Pkcs11Token,
        userPin: String,
        ckaId: CkaIdString,
        keyPairParams: GostKeyPairParams,
        keyPairValidityNotBefore: ZonedDateTime,
        keyPairValidityNotAfter: ZonedDateTime
    ): GostKeyPair {
        return token.openSession(true).use { session ->
            session.login(CKU_USER, userPin).use {
                session.createGostKeyPair(
                    keyPairParams,
                    ckaId.toByteArray(),
                    Pkcs11Date(keyPairValidityNotBefore.toLocalDate()),
                    Pkcs11Date(keyPairValidityNotAfter.toLocalDate())
                )
            }
        }
    }
}
