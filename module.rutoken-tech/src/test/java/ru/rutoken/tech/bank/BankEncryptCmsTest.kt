/*
 * Copyright (c) 2024, Aktiv-Soft JSC.
 * See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.tech.bank

import io.kotest.assertions.throwables.shouldNotThrowAny
import org.bouncycastle.cms.CMSAlgorithm.GOST28147_GCFB
import org.junit.ClassRule
import org.junit.Test
import org.junit.rules.RuleChain
import ru.rutoken.tech.bank.rule.BouncyCastleProviderRule
import ru.rutoken.tech.bouncycastle.BouncyCastleCmsOperations
import ru.rutoken.tech.utils.base64ToX509CertificateHolder

class BankEncryptCmsTest {
    @Test
    fun encrypt() {
        shouldNotThrowAny {
            BouncyCastleCmsOperations.encrypt(
                DATA,
                base64ToX509CertificateHolder(BANK_CERTIFICATE_GOST),
                GOST28147_GCFB
            )
        }
    }

    companion object {
        private val DATA = byteArrayOf(0x01, 0x02, 0x03)
        private val bcProviderRule = BouncyCastleProviderRule()

        @ClassRule
        @JvmField
        val classRule: RuleChain = RuleChain.outerRule(bcProviderRule)
    }
}
