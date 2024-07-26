/*
 * Copyright (c) 2024, Aktiv-Soft JSC.
 * See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.tech.usecasestests.cms

import io.kotest.matchers.shouldBe
import org.bouncycastle.cert.X509CertificateHolder
import org.junit.ClassRule
import org.junit.Test
import org.junit.rules.RuleChain
import ru.rutoken.pkcs11wrapper.constant.standard.Pkcs11UserType.CKU_USER
import ru.rutoken.tech.ca.LocalCA
import ru.rutoken.tech.usecase.CmsOperationProvider
import ru.rutoken.tech.usecase.CmsOperationProvider.BOUNCY_CASTLE
import ru.rutoken.tech.usecase.CmsOperationProvider.PKCS11_WRAPPER
import ru.rutoken.tech.usecase.CmsOperations
import ru.rutoken.tech.usecasestests.DATA
import ru.rutoken.tech.usecasestests.DEFAULT_USER_PIN
import ru.rutoken.tech.usecasestests.appPackageName
import ru.rutoken.tech.usecasestests.makeGostR3410_2012_256KeyPairRule
import ru.rutoken.tech.usecasestests.rule.BouncyCastleProviderRule
import ru.rutoken.tech.usecasestests.rule.CaCertificateRule
import ru.rutoken.tech.usecasestests.rule.CreateGostCertificateRule
import ru.rutoken.tech.usecasestests.rule.LoginRule
import ru.rutoken.tech.usecasestests.rule.PressHomeRule
import ru.rutoken.tech.usecasestests.rule.RtModuleRule
import ru.rutoken.tech.usecasestests.rule.RtSessionRule
import ru.rutoken.tech.usecasestests.rule.RtTokenRule
import ru.rutoken.tech.usecasestests.rule.RtTransportRule
import ru.rutoken.tech.usecasestests.rule.SlotRule
import ru.rutoken.tech.usecasestests.rule.TokenWaitingRule
import ru.rutoken.tech.usecasestests.rule.UsbDevicePermissionRule
import ru.rutoken.tech.utils.VerifyCmsResult

/**
 * This test class should be run with attached USB token. It does not support Bluetooth or NFC tokens.
 * Additionally, the device's screen should be on if test apk does not have USB permission for the token.
 */
class VerifyDetachedGostCmsTest {
    @Test
    fun verifyWrapperCmsViaBouncyCastle() {
        verifyDetached(BOUNCY_CASTLE, signDetached(PKCS11_WRAPPER)) shouldBe VerifyCmsResult.SUCCESS
    }

    @Test
    fun verifyBouncyCastleCmsViaWrapper() {
        verifyDetached(PKCS11_WRAPPER, signDetached(BOUNCY_CASTLE)) shouldBe VerifyCmsResult.SUCCESS
    }

    private fun signDetached(provider: CmsOperationProvider): ByteArray =
        CmsOperations.signDetached(
            provider = provider,
            session = session.value,
            data = DATA,
            signerPrivateKey = keyPair.value.privateKey,
            signerCertificate = certificate.value,
            additionalCertificates = listOf(X509CertificateHolder(caCertificate.encoded)),
        )

    private fun verifyDetached(provider: CmsOperationProvider, detachedCms: ByteArray): VerifyCmsResult =
        CmsOperations.verifyDetached(
            provider = provider,
            cms = detachedCms,
            data = DATA,
            trustedCertificates = listOf(LocalCA.rootCertificate),
            session = session.value,
        )

    companion object {
        private val bcProviderRule = BouncyCastleProviderRule()
        private val pressHomeRule = PressHomeRule()
        private val rtTransport = RtTransportRule()
        private val usbDevicePermission = UsbDevicePermissionRule(appPackageName)
        private val waitForToken = TokenWaitingRule()
        private val module = RtModuleRule()
        private val attributeFactory = module.value.attributeFactory
        private val slot = SlotRule(module)
        private val token = RtTokenRule(slot)
        private val session = RtSessionRule(token)
        private val login = LoginRule(session, CKU_USER, DEFAULT_USER_PIN)
        private val caCertificate = CaCertificateRule(session)
        private val keyPair = attributeFactory.makeGostR3410_2012_256KeyPairRule(session)
        private val certificate = CreateGostCertificateRule(session, keyPair)

        @ClassRule
        @JvmField
        val classRule: RuleChain = RuleChain.outerRule(bcProviderRule)
            .around(pressHomeRule)
            .around(rtTransport)
            .around(usbDevicePermission)
            .around(waitForToken)
            .around(module)
            .around(slot)
            .around(token)
            .around(session)
            .around(login)
            .around(caCertificate)
            .around(keyPair)
            .around(certificate)
    }
}
