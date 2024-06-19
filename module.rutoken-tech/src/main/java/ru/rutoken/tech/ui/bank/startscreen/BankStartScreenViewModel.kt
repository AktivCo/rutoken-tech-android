/*
 * Copyright (c) 2024, Aktiv-Soft JSC.
 * See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.tech.ui.bank.startscreen

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

data class BankUser(
    val id: Int,
    val name: String,
    val position: String,
    val certificateExpirationDate: String,
    val errorText: String? = null
)

class BankStartScreenViewModel : ViewModel() {
    private val _users = MutableLiveData<List<BankUser>>()
    val users: LiveData<List<BankUser>> get() = _users
}