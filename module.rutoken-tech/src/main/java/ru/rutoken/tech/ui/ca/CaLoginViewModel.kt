/*
 * Copyright (c) 2024, Aktiv-Soft JSC.
 * See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.tech.ui.ca

import android.content.Context
import androidx.annotation.MainThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.rutoken.pkcs11wrapper.constant.standard.Pkcs11UserType
import ru.rutoken.tech.R
import ru.rutoken.tech.pkcs11.findobjects.findGost256KeyContainers
import ru.rutoken.tech.pkcs11.serialNumberTrimmed
import ru.rutoken.tech.session.CaRutokenTechSession
import ru.rutoken.tech.session.CkaIdString
import ru.rutoken.tech.session.RutokenTechSessionHolder
import ru.rutoken.tech.tokenmanager.RtPkcs11TokenData
import ru.rutoken.tech.tokenmanager.TokenManager
import ru.rutoken.tech.ui.tokenconnector.TokenConnector
import ru.rutoken.tech.ui.utils.DialogState
import ru.rutoken.tech.ui.utils.callPkcs11Operation
import ru.rutoken.tech.ui.utils.toErrorDialogData
import ru.rutoken.tech.utils.BusinessRuleCase.IncorrectPin
import ru.rutoken.tech.utils.BusinessRuleCase.PinLocked
import ru.rutoken.tech.utils.BusinessRuleException
import ru.rutoken.tech.utils.logd
import ru.rutoken.tech.utils.loge

class CaLoginViewModel(
    private val applicationContext: Context,
    private val tokenManager: TokenManager,
    private val sessionHolder: RutokenTechSessionHolder
) : ViewModel() {
    val tokenConnector = TokenConnector()

    private val _showProgress: MutableLiveData<Boolean> = MutableLiveData(false)
    val showProgress: LiveData<Boolean> get() = _showProgress

    private val _errorDialogState = MutableLiveData(DialogState())
    val errorDialogState: LiveData<DialogState> get() = _errorDialogState

    private val _authDoneEvent = MutableLiveData(false)
    val authDoneEvent: LiveData<Boolean> get() = _authDoneEvent

    @MainThread
    fun onErrorDialogDismiss() {
        _errorDialogState.value = DialogState()
    }

    fun login(tokenUserPin: String, invalidPinBlock: (String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val tokenData = with(tokenManager.getFirstTokenAsync()) {
                    if (isCompleted) getCompleted() else tokenConnector.findFirstToken(tokenManager)
                }

                _showProgress.postValue(true)

                doLogin(tokenUserPin, tokenData)
                    .onFailure { handleLoginFailure(it, invalidPinBlock) }
                    .onSuccess { _authDoneEvent.postValue(true) }
            } finally {
                _showProgress.postValue(false)
            }
        }
    }

    private suspend fun doLogin(tokenUserPin: String, tokenData: RtPkcs11TokenData): Result<CaRutokenTechSession> {
        return runCatching {
            createRutokenTechSession(tokenUserPin, tokenData).also { newSession ->
                sessionHolder.setSession(newSession)
                logd<CaLoginViewModel> { "New CA session created, found ${newSession.keyPairs.size} key pairs" }
            }
        }
    }

    private suspend fun createRutokenTechSession(
        tokenUserPin: String,
        tokenData: RtPkcs11TokenData
    ): CaRutokenTechSession {
        val tokenInfo = tokenData.token.tokenInfo
        var keyPairs: MutableList<CkaIdString> = mutableListOf()

        callPkcs11Operation(_showProgress, tokenManager, tokenInfo.serialNumberTrimmed) {
            tokenData.token.openSession(false).use { session ->
                session.login(Pkcs11UserType.CKU_USER, tokenUserPin).use {
                    keyPairs = session.findGost256KeyContainers().map { it.ckaId.toString(Charsets.UTF_8) }
                        .toMutableList()
                }
            }
        }

        return CaRutokenTechSession(
            tokenUserPin = tokenUserPin,
            tokenSerial = tokenInfo.serialNumberTrimmed,
            tokenModel = tokenData.model,
            tokenLabel = tokenInfo.label,
            keyPairs = keyPairs
        )
    }

    private fun handleLoginFailure(exception: Throwable, invalidPinBlock: (String) -> Unit) {
        loge<CaLoginViewModel>(exception) { "Login has ended with exception: ${exception.message}" }
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
}