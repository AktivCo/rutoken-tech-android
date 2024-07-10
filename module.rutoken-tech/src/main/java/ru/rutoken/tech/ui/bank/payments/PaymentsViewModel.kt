/*
 * Copyright (c) 2024, Aktiv-Soft JSC.
 * See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.tech.ui.bank.payments

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import ru.rutoken.tech.session.AppSessionHolder
import ru.rutoken.tech.session.requireBankUserLoginSession

class PaymentsViewModel(sessionHolder: AppSessionHolder) : ViewModel() {
    private val _payments = MutableLiveData(sessionHolder.requireBankUserLoginSession().payments)
    val payments: LiveData<List<Payment>> get() = _payments
}