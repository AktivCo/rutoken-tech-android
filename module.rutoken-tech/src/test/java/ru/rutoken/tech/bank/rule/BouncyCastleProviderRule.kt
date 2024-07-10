/*
 * Copyright (c) 2024, Aktiv-Soft JSC.
 * See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.tech.bank.rule

import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement
import java.security.Security

class BouncyCastleProviderRule : TestRule {
    override fun apply(base: Statement, description: Description) = object : Statement() {
        override fun evaluate() {
            val bcProvider = BouncyCastleProvider()
            // Remove system Bouncy Castle provider
            Security.removeProvider(bcProvider.name)
            Security.insertProviderAt(bcProvider, 1)

            base.evaluate()
        }
    }
}
