/*
 * Copyright (c) 2024, Aktiv-Soft JSC.
 * See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.tech.ui.bank.payments

import androidx.annotation.MainThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.rutoken.tech.session.AppSessionHolder
import ru.rutoken.tech.session.BankUserLoginAppSession
import ru.rutoken.tech.session.requireBankUserLoginSession

class PaymentsViewModel(private val sessionHolder: AppSessionHolder) : ViewModel() {
    // BankUserLoginAppSession instance MUST exist by the time this ViewModel is instantiated
    private val bankUserLoginSession: BankUserLoginAppSession
        get() = sessionHolder.requireBankUserLoginSession()

    private val _payments = MutableLiveData(bankUserLoginSession.payments)
    val payments: LiveData<List<Payment>> get() = _payments

    @MainThread
    fun onResetPaymentsClicked() {
        viewModelScope.launch(Dispatchers.IO) {
            val payments = bankUserLoginSession.payments.toMutableList()
            payments.replaceAll {
                // We have to create new Payment instance to start the recomposition in UI
                if (it.isArchived()) it.copy(actionTime = null, actionResultData = null) else it
            }
            bankUserLoginSession.payments = payments
            _payments.postValue(payments)
        }
    }
}