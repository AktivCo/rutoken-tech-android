/*
 * Copyright (c) 2024, Aktiv-Soft JSC.
 * See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.tech.ui.bank.payment

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import ru.rutoken.tech.session.AppSessionHolder
import ru.rutoken.tech.session.BankUserLoginAppSession
import ru.rutoken.tech.session.requireBankUserLoginSession
import ru.rutoken.tech.ui.bank.payments.Payment

class PaymentViewModel(private val sessionHolder: AppSessionHolder, private val paymentTitle: String) : ViewModel() {
    //    BankUserLoginAppSession instance MUST exist by the time this ViewModel is instantiated
    private val bankUserLoginSession: BankUserLoginAppSession
        get() = sessionHolder.requireBankUserLoginSession()

    private val _payment = MutableLiveData(bankUserLoginSession.payments.find { it.title == paymentTitle }!!)
    val payment: LiveData<Payment> get() = _payment
}