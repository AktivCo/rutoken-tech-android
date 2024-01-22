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
import ru.rutoken.tech.usecasestests.rule.GenerateKeyPairRule
import ru.rutoken.tech.usecasestests.rule.SessionRule

val appPackageName: String
    get() = InstrumentationRegistry.getInstrumentation().targetContext.packageName

val ID = generateCkaId()

val DATA = byteArrayOf(0x01, 0x02, 0x03)

const val DEFAULT_USER_PIN = "12345678"

val DN = listOf(
    "CN",
    "Ivanoff",
    "C",
    "RU",
    "2.5.4.5",
    "12312312312",
    "1.2.840.113549.1.9.1",
    "ivanov@mail.ru",
    "ST",
    "Moscow"
)

val ATTRIBUTES = listOf(
    "1.7.2.21.1.15.41.43",
    "NULL",
    "1.4.22.43",
    "test string 1",
    "1.4.22.43",
    "Тестовая строка 2"
)

val EXTENSIONS = listOf(
    "keyUsage",
    "digitalSignature,nonRepudiation,keyEncipherment,dataEncipherment",
    "extendedKeyUsage",
    "1.2.643.2.2.34.6,1.3.6.1.5.5.7.3.2,1.3.6.1.5.5.7.3.4"
)

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
