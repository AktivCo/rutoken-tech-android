/*
 * Copyright (c) 2024, Aktiv-Soft JSC.
 * See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.tech.bank

import io.kotest.assertions.throwables.shouldNotThrowAny
import org.bouncycastle.cert.X509CertificateHolder
import org.bouncycastle.cms.CMSAlgorithm.GOST28147_GCFB
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.junit.After
import org.junit.Before
import org.junit.Test
import ru.rutoken.tech.bouncycastle.BouncyCastleCmsOperations
import java.security.Security
import java.util.Base64

class BankEncryptCmsTest {
    private val bcProvider = BouncyCastleProvider()

    @Before
    fun setUpSecurityProvider() {
        // Remove system Bouncy Castle provider
        Security.removeProvider(bcProvider.name)
        Security.insertProviderAt(bcProvider, 1)
    }

    @After
    fun removeSecurityProvider() {
        Security.removeProvider(bcProvider.name)
    }

    @Test
    fun encrypt() {
        shouldNotThrowAny {
            val bankCertificate = Base64.getDecoder().decode(BANK_CERTIFICATE_GOST)
            val bankCertificateHolder = X509CertificateHolder(bankCertificate)

            BouncyCastleCmsOperations.encrypt(DATA, bankCertificateHolder, GOST28147_GCFB)
        }
    }

    companion object {
        private val DATA = byteArrayOf(0x01, 0x02, 0x03)
    }
}
