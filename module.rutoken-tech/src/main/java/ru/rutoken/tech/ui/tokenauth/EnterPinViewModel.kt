/*
 * Copyright (c) 2024, Aktiv-Soft JSC.
 * See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.tech.ui.tokenauth

import android.content.Context
import androidx.annotation.MainThread
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.rutoken.tech.R
import ru.rutoken.tech.bank.biometry.decryptWithBiometricPrompt
import ru.rutoken.tech.bank.biometry.encryptWithBiometricPrompt
import ru.rutoken.tech.repository.bank.BankUserRepository
import ru.rutoken.tech.session.AppSessionHolder
import ru.rutoken.tech.session.bankUserLoginSession
import ru.rutoken.tech.session.requireBankUserLoginSession
import ru.rutoken.tech.ui.utils.DialogState
import ru.rutoken.tech.ui.utils.ErrorDialogData

private val failedBiometryUseDialogData =
    ErrorDialogData(R.string.failed_biometry_use_title, R.string.failed_biometry_use_text)

private val failedBiometryActivationDialogData =
    ErrorDialogData(R.string.biometry_activation_failed_title, R.string.biometry_activation_failed_text)

class EnterPinViewModel(
    applicationContext: Context,
    private val sessionHolder: AppSessionHolder,
    private val repository: BankUserRepository
) : ViewModel() {
    private val _isButtonEnabled = MutableLiveData(false)
    val isButtonEnabled: LiveData<Boolean> get() = _isButtonEnabled

    private val _pinValue = MutableLiveData("")
    val pinValue: LiveData<String> get() = _pinValue

    private val _pinErrorText = MutableLiveData("")
    val pinErrorText: LiveData<String?> get() = _pinErrorText

    private val _hasBiometricPin = MutableLiveData(hasBiometricPin(applicationContext))
    val hasBiometricPin: LiveData<Boolean> get() = _hasBiometricPin

    private val _biometryErrorDialogState = MutableLiveData(DialogState())
    val biometryErrorDialogState: LiveData<DialogState> get() = _biometryErrorDialogState

    private val _biometryUpdateDone = MutableLiveData(false)
    val biometryUpdateDone: LiveData<Boolean> get() = _biometryUpdateDone

    @MainThread
    fun onPinValueChanged(pinValue: String) {
        _pinValue.value = pinValue
        _pinErrorText.value = null
        _isButtonEnabled.value = pinValue.isNotEmpty()
    }

    fun onInvalidPin(errorText: String) {
        viewModelScope.launch(Dispatchers.IO) {
            deleteBiometricPin()
            _isButtonEnabled.postValue(false)
            _pinErrorText.postValue(errorText)
        }
    }

    fun onValidPinLogin(fragmentActivity: FragmentActivity) {
        val loginSession = sessionHolder.bankUserLoginSession

        if (loginSession != null && loginSession.isBiometryActive && loginSession.encryptedPinData == null) {
            viewModelScope.launch(Dispatchers.IO) {
                encryptWithBiometricPrompt(
                    activity = fragmentActivity,
                    data = _pinValue.value!!.toByteArray(),
                    onError = { _, _ ->
                        _biometryErrorDialogState.postValue(
                            DialogState(showDialog = true, data = failedBiometryActivationDialogData)
                        )
                    },
                    onSuccess = { encryptedPin, cipherIv ->
                        viewModelScope.launch(Dispatchers.IO) {
                            repository.updateUserEncryptedPin(loginSession.userId, encryptedPin, cipherIv)
                            _biometryUpdateDone.postValue(true)
                        }
                    }
                )
            }
        } else {
            _biometryUpdateDone.postValue(true)
        }
    }

    fun dismissBiometryErrorDialog(dialogData: ErrorDialogData) {
        _biometryErrorDialogState.postValue(DialogState(showDialog = false))
        if (dialogData === failedBiometryActivationDialogData)
            _biometryUpdateDone.postValue(true)
    }

    fun fillPinWithBiometricPrompt(fragmentActivity: FragmentActivity) {
        val encryptedPinData = sessionHolder.requireBankUserLoginSession().encryptedPinData!!

        viewModelScope.launch(Dispatchers.IO) {
            decryptWithBiometricPrompt(
                activity = fragmentActivity,
                data = encryptedPinData.bytes,
                cipherIv = encryptedPinData.cipherIv,
                onError = { _, _ ->
                    _biometryErrorDialogState.postValue(
                        DialogState(showDialog = true, data = failedBiometryUseDialogData)
                    )
                },
                onSuccess = {
                    _pinValue.postValue(String(it, Charsets.UTF_8))
                    _isButtonEnabled.postValue(true)
                }
            )
        }
    }

    private fun hasBiometricPin(context: Context): Boolean {
        val bankUserLoginSession = sessionHolder.bankUserLoginSession ?: return false

        return bankUserLoginSession.hasPinToDecrypt(context)
    }

    private suspend fun deleteBiometricPin() {
        sessionHolder.bankUserLoginSession?.let {
            if (it.isBiometryActive) {
                it.encryptedPinData = null
                repository.deleteUserEncryptedPin(it.userId)
            }
        }
    }
}