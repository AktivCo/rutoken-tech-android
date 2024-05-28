/*
 * Copyright (c) 2024, Aktiv-Soft JSC.
 * See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.tech.usecasestests.cms

import io.kotest.matchers.shouldBe
import org.bouncycastle.cert.X509CertificateHolder
import org.bouncycastle.cms.CMSAlgorithm.GOST28147_GCFB
import org.junit.ClassRule
import org.junit.Test
import org.junit.rules.RuleChain
import ru.rutoken.pkcs11wrapper.constant.standard.Pkcs11UserType
import ru.rutoken.tech.bouncycastle.BouncyCastleCmsOperations
import ru.rutoken.tech.usecasestests.DATA
import ru.rutoken.tech.usecasestests.DEFAULT_USER_PIN
import ru.rutoken.tech.usecasestests.appPackageName
import ru.rutoken.tech.usecasestests.makeGostR3410_2012_256KeyPairRule
import ru.rutoken.tech.usecasestests.rule.BouncyCastleProviderRule
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

/**
 * This test class should be run with attached USB token. It does not support Bluetooth or NFC tokens.
 * Additionally, the device's screen should be on if test apk does not have USB permission for the token.
 */
class EncryptDecryptGostTest {
    @Test
    fun encryptDecrypt() {
        val certificateHolder = X509CertificateHolder(certificate.encoded)
        val encryptedData = BouncyCastleCmsOperations.encrypt(DATA, certificateHolder, GOST28147_GCFB)

        val certificates = listOf(certificateHolder)
        val decryptedData =
            BouncyCastleCmsOperations.decrypt(session.value, encryptedData, certificates, keyPair.value.privateKey)

        decryptedData shouldBe DATA
    }

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
        private val login = LoginRule(session, Pkcs11UserType.CKU_USER, DEFAULT_USER_PIN)
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
            .around(keyPair)
            .around(certificate)
    }
}
