/*
 * Copyright (c) 2024, Aktiv-Soft JSC.
 * See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.tech.usecasestests.rule

import org.junit.rules.ExternalResource
import ru.rutoken.pkcs11wrapper.constant.standard.Pkcs11UserType
import ru.rutoken.pkcs11wrapper.main.Pkcs11Session

class LoginRule(private val session: SessionRule, private val userType: Pkcs11UserType, private val pin: String) :
    ExternalResource() {
    private lateinit var _value: Pkcs11Session.LoginGuard
    val value get() = _value

    override fun before() {
        _value = session.value.login(userType, pin)
    }

    override fun after() {
        _value.close()
    }
}
