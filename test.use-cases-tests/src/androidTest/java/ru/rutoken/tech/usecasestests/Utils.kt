package ru.rutoken.tech.usecasestests

import androidx.test.platform.app.InstrumentationRegistry
import ru.rutoken.pkcs11wrapper.attribute.IPkcs11AttributeFactory
import ru.rutoken.pkcs11wrapper.mechanism.Pkcs11Mechanism
import ru.rutoken.pkcs11wrapper.`object`.key.Pkcs11GostPrivateKeyObject
import ru.rutoken.pkcs11wrapper.`object`.key.Pkcs11GostPublicKeyObject
import ru.rutoken.tech.pkcs11.createobjects.GostKeyPairParams
import ru.rutoken.tech.pkcs11.createobjects.generateCkaId
import ru.rutoken.tech.pkcs11.createobjects.makeGostPrivateKeyTemplate
import ru.rutoken.tech.pkcs11.createobjects.makeGostPublicKeyTemplate
import ru.rutoken.tech.ui.ca.generateobjects.certificate.createDN
import ru.rutoken.tech.usecasestests.rule.GenerateKeyPairRule
import ru.rutoken.tech.usecasestests.rule.SessionRule

val appPackageName: String
    get() = InstrumentationRegistry.getInstrumentation().targetContext.packageName

val ID = generateCkaId()

val DATA = byteArrayOf(0x01, 0x02, 0x03)

const val DEFAULT_USER_PIN = "12345678"

fun IPkcs11AttributeFactory.makeGostR3410KeyPairRule(
    sessionRule: SessionRule,
    keyPairParams: GostKeyPairParams,
    ckaId: ByteArray,
) = GenerateKeyPairRule(
    Pkcs11GostPublicKeyObject::class.java,
    Pkcs11GostPrivateKeyObject::class.java,
    sessionRule,
    Pkcs11Mechanism.make(keyPairParams.mechanismType),
    makeGostPublicKeyTemplate(keyPairParams, ckaId),
    makeGostPrivateKeyTemplate(keyPairParams, ckaId),
)

fun IPkcs11AttributeFactory.makeGostR3410_2012_256KeyPairRule(sessionRule: SessionRule) =
    makeGostR3410KeyPairRule(sessionRule, GostKeyPairParams.GOST_2012_256, ID)
