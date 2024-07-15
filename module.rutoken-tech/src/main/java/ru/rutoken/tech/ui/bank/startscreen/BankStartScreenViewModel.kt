/*
 * Copyright (c) 2024, Aktiv-Soft JSC.
 * See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.tech.ui.bank.startscreen

import android.content.Context
import androidx.annotation.MainThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.rutoken.tech.repository.user.UserRepository
import ru.rutoken.tech.session.AppSessionHolder
import ru.rutoken.tech.session.BankUserLoginAppSession
import ru.rutoken.tech.ui.bank.BankUser
import ru.rutoken.tech.ui.tokenauth.LoginViewModel
import ru.rutoken.tech.ui.utils.getCertificateErrorText
import ru.rutoken.tech.utils.logd
import ru.rutoken.tech.utils.toDateString

class BankStartScreenViewModel(
    private val applicationContext: Context,
    private val repository: UserRepository,
    private val sessionHolder: AppSessionHolder
) : ViewModel() {
    private val _users = MutableLiveData<List<BankUser>>()
    val users: LiveData<List<BankUser>> get() = _users

    private val _showDeleteUsersDialog = MutableLiveData(false)
    val showDeleteUsersDialog: LiveData<Boolean> get() = _showDeleteUsersDialog

    private val _loginAppSessionLoaded = MutableLiveData(false)
    val loginAppSessionLoaded: LiveData<Boolean> get() = _loginAppSessionLoaded

    fun loadUsers() {
        viewModelScope.launch(Dispatchers.IO) {
            _users.postValue(repository.getUsers().map { user ->
                BankUser(
                    id = user.userEntity.id,
                    name = user.fullName,
                    position = user.position,
                    certificateExpirationDate = user.certificateNotAfter.toDateString(),
                    errorText = applicationContext.getCertificateErrorText(
                        user.certificateNotBefore, user.certificateNotAfter
                    )
                )
            }.toMutableList().apply { sortBy { it.errorText != null } })
        }
    }

    fun deleteAllUsers() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteAllUsers()
            sessionHolder.resetSession()
            _showDeleteUsersDialog.postValue(false)
            _users.postValue(emptyList())
        }
    }

    @MainThread
    fun onShowDeleteUsersDialog() {
        _showDeleteUsersDialog.value = true
    }

    @MainThread
    fun onDismissDeleteUsersDialog() {
        _showDeleteUsersDialog.value = false
    }

    fun onNavigateToUserLogin() {
        _loginAppSessionLoaded.postValue(false)
    }

    fun onNavigateToAddUser() {
        sessionHolder.resetSession()
    }

    @MainThread
    fun onUserClicked(user: BankUser) {
        viewModelScope.launch(Dispatchers.IO) {
            with(repository.getUser(user.id).userEntity) {
                val pinData =
                    if (encryptedPin != null && cipherIv != null)
                        BankUserLoginAppSession.EncryptedPinData(encryptedPin, cipherIv)
                    else
                        null

                val userLoginAppSession = BankUserLoginAppSession(
                    id,
                    tokenSerialNumber,
                    ckaId,
                    certificateDerValue,
                    isBiometryActive,
                    pinData
                )
                sessionHolder.setSession(userLoginAppSession)
                logd<BankStartScreenViewModel> { "New BankUserLogin session created for userId $id" }
            }
            _loginAppSessionLoaded.postValue(true)
        }
    }
}