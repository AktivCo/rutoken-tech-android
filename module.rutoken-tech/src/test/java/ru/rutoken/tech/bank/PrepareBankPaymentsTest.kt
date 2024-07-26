/*
 * Copyright (c) 2024, Aktiv-Soft JSC.
 * See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.tech.bank

import io.kotest.matchers.shouldBe
import org.bouncycastle.cert.X509CertificateHolder
import org.junit.ClassRule
import org.junit.Test
import org.junit.rules.RuleChain
import ru.rutoken.tech.bank.rule.BouncyCastleProviderRule
import ru.rutoken.tech.bank.rule.MockAndroidLogRule
import ru.rutoken.tech.bouncycastle.BouncyCastleCmsOperations
import ru.rutoken.tech.ca.LocalCA
import ru.rutoken.tech.usecase.CmsOperationProvider
import ru.rutoken.tech.usecase.CmsOperations
import ru.rutoken.tech.utils.VerifyCmsResult
import ru.rutoken.tech.utils.base64ToX509CertificateHolder

class PrepareBankPaymentsTest {
    @Test
    fun signVerifySuccess() {
        val detachedCms = BouncyCastleCmsOperations.signDetachedGost256(
            DATA,
            BANK_PRIVATE_KEY_GOST,
            base64ToX509CertificateHolder(BANK_CERTIFICATE_GOST),
            listOf(X509CertificateHolder(LocalCA.caCertificate))
        )

        verifyDetached(detachedCms) shouldBe VerifyCmsResult.SUCCESS
    }

    @Test
    fun signVerifyCertificateChainNotVerified() {
        val detachedCms = BouncyCastleCmsOperations.signDetachedGost256(
            DATA,
            BANK_PRIVATE_KEY_GOST,
            base64ToX509CertificateHolder(BANK_CERTIFICATE_GOST),
            emptyList()
        )

        verifyDetached(detachedCms) shouldBe VerifyCmsResult.CERTIFICATE_CHAIN_NOT_VERIFIED
    }

    @Test
    fun signVerifyInvalidSignature() {
        val detachedCms = BouncyCastleCmsOperations.signDetachedGost256(
            DATA + 1,
            BANK_PRIVATE_KEY_GOST,
            base64ToX509CertificateHolder(BANK_CERTIFICATE_GOST),
            listOf(X509CertificateHolder(LocalCA.caCertificate))
        )

        verifyDetached(detachedCms) shouldBe VerifyCmsResult.SIGNATURE_INVALID
    }

    private fun verifyDetached(detachedCms: ByteArray): VerifyCmsResult =
        CmsOperations.verifyDetached(
            provider = CmsOperationProvider.BOUNCY_CASTLE,
            cms = detachedCms,
            data = DATA,
            trustedCertificates = listOf(LocalCA.rootCertificate),
        )

    companion object {
        private val DATA = byteArrayOf(0x01, 0x02, 0x03)
        private val bcProviderRule = BouncyCastleProviderRule()
        private val mockAndroidLogRule = MockAndroidLogRule()

        @ClassRule
        @JvmField
        val classRule: RuleChain = RuleChain.outerRule(bcProviderRule).around(mockAndroidLogRule)
    }
}
