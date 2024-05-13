package ru.rutoken.tech.usecasestests.rule

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
