/*
 * Copyright (c) 2024, Aktiv-Soft JSC.
 * See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.tech.ui.bank.startscreen

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.rutoken.tech.repository.user.UserRepository
import ru.rutoken.tech.ui.bank.BankUser
import ru.rutoken.tech.ui.utils.getCertificateErrorText
import ru.rutoken.tech.utils.toDateString

class BankStartScreenViewModel(
    private val applicationContext: Context,
    private val repository: UserRepository
) : ViewModel() {
    private val _users = MutableLiveData<List<BankUser>>()
    val users: LiveData<List<BankUser>> get() = _users

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
}