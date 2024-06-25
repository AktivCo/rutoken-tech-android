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
import ru.rutoken.tech.ui.bank.BankUser
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.rutoken.tech.R
import ru.rutoken.tech.repository.user.UserRepository
import ru.rutoken.tech.utils.toDateString
import java.util.Date

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
                    errorText = getErrorText(user.certificateNotBefore, user.certificateNotAfter)
                )
            })
        }
    }

    private fun getErrorText(certificateNotBefore: Date, certificateNotAfter: Date): String? {
        val currentDate = Date()
        return if (currentDate.after(certificateNotAfter))
            applicationContext.getString(R.string.certificate_is_expired)
        else if (certificateNotBefore.after(currentDate))
            applicationContext.getString(R.string.certificate_not_yet_valid, certificateNotBefore.toDateString())
        else
            null
    }
}