/*
 * Copyright (c) 2024, Aktiv-Soft JSC.
 * See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.tech.usecasestests.rule

import androidx.test.platform.app.InstrumentationRegistry
import io.kotest.matchers.shouldBe
import org.junit.rules.ExternalResource
import ru.rutoken.rtpcscbridge.RtPcscBridge
import ru.rutoken.rttransport.RtTransport

class RtTransportRule : ExternalResource() {
    private val context = InstrumentationRegistry.getInstrumentation().context

    private lateinit var _value: RtTransport
    val value get() = _value

    override fun before() {
        RtPcscBridge.enableDebugLogs()
        RtPcscBridge.setAppContext(context) shouldBe true
        _value = RtPcscBridge.getTransport()
        _value.initialize(context) shouldBe true
    }

    override fun after() {
        _value.finalize(context) shouldBe true
    }
}
