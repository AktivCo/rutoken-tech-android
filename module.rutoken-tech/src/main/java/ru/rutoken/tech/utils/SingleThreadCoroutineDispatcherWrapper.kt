/*
 * Copyright (c) 2024, Aktiv-Soft JSC.
 * See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.tech.utils

import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.delay
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.coroutines.CoroutineContext

private const val EXECUTOR_STATE_CHECK_DELAY = 100L

class SingleThreadCoroutineDispatcherWrapper {
    private val coroutineDispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher()

    val context: CoroutineContext get() = coroutineDispatcher

    suspend fun closeAndWaitForTerminating() {
        coroutineDispatcher.close()
        while (!(coroutineDispatcher.executor as ExecutorService).isTerminated) {
            delay(EXECUTOR_STATE_CHECK_DELAY)
        }
    }
}
