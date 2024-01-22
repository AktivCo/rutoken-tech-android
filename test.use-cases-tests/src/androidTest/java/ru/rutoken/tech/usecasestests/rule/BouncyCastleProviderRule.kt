package ru.rutoken.tech.usecasestests.rule

import org.bouncycastle.asn1.rosstandart.RosstandartObjectIdentifiers
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement
import ru.rutoken.tech.bouncycastle.Gost2012KeyFactorySpi
import java.security.Security

class BouncyCastleProviderRule : TestRule {
    override fun apply(base: Statement, description: Description) = object : Statement() {
        override fun evaluate() {
            val bcProvider = BouncyCastleProvider()
            // TODO: Remove when the Bouncy Castle issue [#1586](https://github.com/bcgit/bc-java/issues/1586) is
            //  resolved.
            bcProvider.addKeyInfoConverter(
                RosstandartObjectIdentifiers.id_tc26_gost_3410_12_256,
                Gost2012KeyFactorySpi()
            )
            // Remove system Bouncy Castle provider
            Security.removeProvider(bcProvider.name)
            Security.insertProviderAt(bcProvider, 1)

            base.evaluate()
        }
    }
}
