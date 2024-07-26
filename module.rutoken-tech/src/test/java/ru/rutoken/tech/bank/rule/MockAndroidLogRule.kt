/*
 * Copyright (c) 2024, Aktiv-Soft JSC.
 * See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.tech.bank.rule

import android.util.Log
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

/**
 * Mocks the [android.util.Log] class and some of its methods, because in tested methods we use functions from the
 * Logger.kt file, and calling class methods from the [android] package in unit tests causes an exception.
 */
class MockAndroidLogRule : TestRule {
    override fun apply(base: Statement, description: Description) = object : Statement() {
        override fun evaluate() {
            try {
                mockkStatic(Log::class)
                every { Log.println(any(), any(), any()) } returns 0
                every { Log.getStackTraceString(any()) } returns "Mock stack trace string"
                base.evaluate()
            } finally {
                unmockkAll()
            }
        }
    }
}
