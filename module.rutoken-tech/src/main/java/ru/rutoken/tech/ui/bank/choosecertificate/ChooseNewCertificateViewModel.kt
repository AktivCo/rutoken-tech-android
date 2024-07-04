/*
 * Copyright (c) 2024, Aktiv-Soft JSC.
 * See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.tech.ui.bank.choosecertificate

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
import ru.rutoken.tech.bank.biometry.encryptWithBiometricPrompt
import ru.rutoken.tech.bank.biometry.shouldAskForBiometry
import ru.rutoken.tech.database.user.UserEntity
import ru.rutoken.tech.repository.user.UserRepository
import ru.rutoken.tech.repository.user.makeUser
import ru.rutoken.tech.session.AppSessionHolder
import ru.rutoken.tech.session.BankUserAddingAppSession
import ru.rutoken.tech.session.requireBankUserAddingSession
import ru.rutoken.tech.ui.bank.BankCertificate
import ru.rutoken.tech.ui.utils.DialogState
import ru.rutoken.tech.ui.utils.ErrorDialogData
import ru.rutoken.tech.utils.loge

class ChooseNewCertificateViewModel(
    private val applicationContext: Context,
    private val sessionHolder: AppSessionHolder,
    private val repository: UserRepository
) : ViewModel() {
    //    BankUserAddingAppSession instance MUST exist by the time this ViewModel is instantiated
    private val bankUserAddingAppSession: BankUserAddingAppSession
        get() = sessionHolder.requireBankUserAddingSession()

    private lateinit var chosenCertificate: BankCertificate

    private val _askBiometryDialog = MutableLiveData(false)
    val askBiometryDialog: LiveData<Boolean> get() = _askBiometryDialog

    private val _biometryActivationFailedDialogState = MutableLiveData<DialogState>()
    val biometryActivationFailedDialogState: LiveData<DialogState> get() = _biometryActivationFailedDialogState

    private val _certificates = MutableLiveData(bankUserAddingAppSession.certificates)
    val certificates: LiveData<List<BankCertificate>> get() = _certificates

    private val _isUserAdded = MutableLiveData(false)
    val isUserAdded: LiveData<Boolean> get() = _isUserAdded

    @MainThread
    fun onCertificateClicked(certificate: BankCertificate) {
        chosenCertificate = certificate

        if (applicationContext.shouldAskForBiometry())
            _askBiometryDialog.value = true
        else
            saveUserToDatabase()
    }

    @MainThread
    fun onDismissBiometryDialog() {
        _askBiometryDialog.value = false
        saveUserToDatabase()
    }

    @MainThread
    fun onDismissBiometryActivationFailedDialog() {
        _biometryActivationFailedDialogState.value = DialogState(showDialog = false)
        saveUserToDatabase()
    }

    @MainThread
    fun onConfirmBiometryUsage(activity: FragmentActivity) {
        _askBiometryDialog.value = false
        viewModelScope.launch(Dispatchers.IO) {
            encryptWithBiometricPrompt(
                activity = activity,
                data = bankUserAddingAppSession.tokenUserPin.toByteArray(),
                onError = { errorCode, errorMessage ->
                    val errorText = if (errorMessage != null) ": $errorMessage" else ""
                    loge { "Biometric authentication failed with code $errorCode$errorText" }

                    val dialogData = ErrorDialogData(
                        title = R.string.biometry_activation_failed_title,
                        text = R.string.biometry_activation_failed_text
                    )
                    _biometryActivationFailedDialogState.postValue(DialogState(true, dialogData))
                },
                onNonRecognized = { /* Nothing to do - a user has more attempts left */ },
                onSuccess = { encryptedPin -> saveUserToDatabase(encryptedPin) }
            )
        }
    }

    private fun saveUserToDatabase(encryptedPin: ByteArray? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addUser(
                makeUser(
                    UserEntity(
                        certificateDerValue = chosenCertificate.bytes,
                        ckaId = chosenCertificate.ckaId,
                        tokenSerialNumber = bankUserAddingAppSession.tokenSerial,
                        encryptedPin = encryptedPin
                    )
                )
            )
            sessionHolder.resetSession()
            _isUserAdded.postValue(true)
        }
    }
}