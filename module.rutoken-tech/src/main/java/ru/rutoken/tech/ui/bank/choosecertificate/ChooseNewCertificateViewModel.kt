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
import org.bouncycastle.cert.X509CertificateHolder
import ru.rutoken.tech.R
import ru.rutoken.tech.bank.biometry.encryptWithBiometricPrompt
import ru.rutoken.tech.bank.biometry.canUseBiometry
import ru.rutoken.tech.database.user.UserEntity
import ru.rutoken.tech.repository.UserRepository
import ru.rutoken.tech.repository.makeUser
import ru.rutoken.tech.session.AppSessionHolder
import ru.rutoken.tech.session.BankUserAddingAppSession
import ru.rutoken.tech.session.BankUserLoginAppSession
import ru.rutoken.tech.session.requireBankUserAddingSession
import ru.rutoken.tech.ui.bank.BankCertificate
import ru.rutoken.tech.ui.bank.payments.getInitialPaymentsStorage
import ru.rutoken.tech.ui.utils.DialogState
import ru.rutoken.tech.ui.utils.ErrorDialogData

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

    private val _showProgress = MutableLiveData<Boolean>()
    val showProgress: LiveData<Boolean> = _showProgress

    @MainThread
    fun onCertificateClicked(certificate: BankCertificate) {
        chosenCertificate = certificate

        if (applicationContext.canUseBiometry())
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
                onError = { _, _ ->
                    val dialogData = ErrorDialogData(
                        title = R.string.biometry_activation_failed_title,
                        text = R.string.biometry_activation_failed_text
                    )
                    _biometryActivationFailedDialogState.postValue(DialogState(true, dialogData))
                },
                onSuccess = { encryptedPin, cipherIv -> saveUserToDatabase(encryptedPin, cipherIv) }
            )
        }
    }

    private fun saveUserToDatabase(encryptedPin: ByteArray? = null, cipherIv: ByteArray? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            _showProgress.postValue(true)

            val tokenSerial = bankUserAddingAppSession.tokenSerial
            val user = makeUser(
                UserEntity(
                    certificateDerValue = chosenCertificate.bytes,
                    ckaId = chosenCertificate.ckaId,
                    tokenSerialNumber = tokenSerial,
                    isBiometryActive = encryptedPin != null,
                    encryptedPin = encryptedPin,
                    cipherIv = cipherIv
                )
            )
            repository.addUser(user)

            val paymentsStorage =
                getInitialPaymentsStorage(applicationContext, X509CertificateHolder(chosenCertificate.bytes))
            val pinData =
                if (encryptedPin != null && cipherIv != null)
                    BankUserLoginAppSession.EncryptedPinData(encryptedPin, cipherIv)
                else
                    null

            sessionHolder.setSession(
                BankUserLoginAppSession(
                    userId = user.userEntity.id,
                    tokenSerial = tokenSerial,
                    certificateCkaId = chosenCertificate.ckaId,
                    certificate = chosenCertificate.bytes,
                    isBiometryActive = encryptedPin != null,
                    encryptedPinData = pinData,
                    payments = paymentsStorage
                )
            )

            _showProgress.postValue(false)
            _isUserAdded.postValue(true)
        }
    }
}