/*
 * Copyright (c) 2024, Aktiv-Soft JSC.
 * See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.tech.session

import ru.rutoken.tech.utils.logv
import java.util.concurrent.atomic.AtomicReference

class AppSessionHolder {
    private val _session: AtomicReference<AppSession> = AtomicReference()

    val session: AppSession?
        get() = _session.get()

    fun setSession(newSession: AppSession) {
        _session.set(newSession)
    }

    fun resetSession() {
        logv<AppSession> { "Clean up app session" }
        _session.set(null)
    }
}

val AppSessionHolder.caSession: CaAppSession?
    get() = session as? CaAppSession

val AppSessionHolder.bankUserAddingSession: BankUserAddingAppSession?
    get() = session as? BankUserAddingAppSession

val AppSessionHolder.bankUserLoginSession: BankUserLoginAppSession?
    get() = session as? BankUserLoginAppSession

fun AppSessionHolder.requireCaSession(): CaAppSession = requireNotNull(caSession)

fun AppSessionHolder.requireBankUserAddingSession(): BankUserAddingAppSession = requireNotNull(bankUserAddingSession)

fun AppSessionHolder.requireBankUserLoginSession(): BankUserLoginAppSession = requireNotNull(bankUserLoginSession)