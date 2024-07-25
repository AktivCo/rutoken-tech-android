/*
 * Copyright (c) 2024, Aktiv-Soft JSC.
 * See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.tech.ui.bank.payment

import android.content.Context
import androidx.annotation.MainThread
import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import ru.rutoken.pkcs11wrapper.rutoken.main.RtPkcs11Session
import ru.rutoken.tech.R
import ru.rutoken.tech.session.AppSessionHolder
import ru.rutoken.tech.session.BankUserLoginAppSession
import ru.rutoken.tech.session.requireBankUserLoginSession
import ru.rutoken.tech.ui.bank.payments.Payment
import ru.rutoken.tech.ui.bank.payments.UserActionType
import ru.rutoken.tech.ui.utils.DialogData
import ru.rutoken.tech.ui.utils.DialogState

class PaymentViewModel(
    private val applicationContext: Context,
    private val sessionHolder: AppSessionHolder,
    private val paymentTitle: String
) : ViewModel() {
    // BankUserLoginAppSession instance MUST exist by the time this ViewModel is instantiated
    private val bankUserLoginSession: BankUserLoginAppSession
        get() = sessionHolder.requireBankUserLoginSession()

    private val _payment = MutableLiveData(bankUserLoginSession.payments.first { it.title == paymentTitle }.also {
        if (it.userActionType == UserActionType.SIGN) // TODO: add another operations
            bankUserLoginSession.operationWithToken = ::signPaymentOnTokenAuth
    })
    val payment: LiveData<Payment> get() = _payment

    private val _operationCompletedDialogState = MutableLiveData<DialogState>()
    val operationCompletedDialogState: LiveData<DialogState> get() = _operationCompletedDialogState

    @WorkerThread
    private fun signPaymentOnTokenAuth(session: RtPkcs11Session) {
        session.signPayment(_payment.value!!, bankUserLoginSession.certificateCkaId, applicationContext)
        _operationCompletedDialogState.postValue(DialogState(true, DialogData(R.string.sign_operation_completed)))
    }

    @MainThread
    fun onDismissOperationCompletedDialog() {
        _operationCompletedDialogState.value = DialogState(showDialog = false)
    }
}