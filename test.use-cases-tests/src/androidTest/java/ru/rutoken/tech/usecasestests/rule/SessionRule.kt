/*
 * Copyright (c) 2024, Aktiv-Soft JSC.
 * See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.tech.usecasestests.rule

import io.kotest.matchers.types.shouldBeSameInstanceAs
import org.junit.rules.ExternalResource
import ru.rutoken.pkcs11wrapper.main.Pkcs11Session
import ru.rutoken.pkcs11wrapper.rutoken.main.RtPkcs11Session

open class SessionRule(private val token: TokenRule) : ExternalResource() {
    private lateinit var _value: Pkcs11Session
    open val value get() = _value

    override fun before() {
        _value = token.value.openSession(true)
        _value.token shouldBeSameInstanceAs token.value
    }

    override fun after() {
        _value.close()
    }
}

class RtSessionRule(token: TokenRule) : SessionRule(token) {
    override val value get() = super.value as RtPkcs11Session
}
