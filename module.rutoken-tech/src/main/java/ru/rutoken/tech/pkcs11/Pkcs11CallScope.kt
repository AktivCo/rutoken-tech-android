/*
 * Copyright (c) 2024, Aktiv-Soft JSC.
 * See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.tech.pkcs11

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.withContext
import ru.rutoken.tech.utils.SingleThreadCoroutineDispatcherWrapper
import ru.rutoken.tech.utils.logd
import java.util.concurrent.atomic.AtomicReference

object Pkcs11CallScope {
    private val dispatcher = AtomicReference<SingleThreadCoroutineDispatcherWrapper>()

    fun initPkcs11CallContext() {
        logd { "Initializing PKCS#11 operations dispatcher" }
        if (!dispatcher.compareAndSet(null, SingleThreadCoroutineDispatcherWrapper()))
            logd { "PKCS#11 operations dispatcher already initialized" }
    }

    suspend fun <T> withPkcs11CallContext(block: suspend CoroutineScope.() -> T): T {
        with(dispatcher.get()) {
            if (this == null)
                throw IllegalStateException("PKCS#11 operations dispatcher has not been initialized")

            return withContext(context) { block() }
        }
    }

    suspend fun closePkcs11CallContext() {
        logd { "Closing PKCS#11 operations dispatcher" }
        dispatcher.getAndSet(null)?.closeAndWaitForTerminating()
    }
}
