package ru.rutoken.tech.pkcs11.createobjects

import ru.rutoken.pkcs11wrapper.attribute.IPkcs11AttributeFactory
import ru.rutoken.pkcs11wrapper.attribute.Pkcs11Attribute
import ru.rutoken.pkcs11wrapper.constant.IPkcs11MechanismType
import ru.rutoken.pkcs11wrapper.constant.standard.Pkcs11AttributeType.CKA_CLASS
import ru.rutoken.pkcs11wrapper.constant.standard.Pkcs11AttributeType.CKA_DECRYPT
import ru.rutoken.pkcs11wrapper.constant.standard.Pkcs11AttributeType.CKA_ENCRYPT
import ru.rutoken.pkcs11wrapper.constant.standard.Pkcs11AttributeType.CKA_GOSTR3410_PARAMS
import ru.rutoken.pkcs11wrapper.constant.standard.Pkcs11AttributeType.CKA_GOSTR3411_PARAMS
import ru.rutoken.pkcs11wrapper.constant.standard.Pkcs11AttributeType.CKA_ID
import ru.rutoken.pkcs11wrapper.constant.standard.Pkcs11AttributeType.CKA_KEY_TYPE
import ru.rutoken.pkcs11wrapper.constant.standard.Pkcs11AttributeType.CKA_PRIVATE
import ru.rutoken.pkcs11wrapper.constant.standard.Pkcs11AttributeType.CKA_SIGN
import ru.rutoken.pkcs11wrapper.constant.standard.Pkcs11AttributeType.CKA_TOKEN
import ru.rutoken.pkcs11wrapper.constant.standard.Pkcs11AttributeType.CKA_VERIFY
import ru.rutoken.pkcs11wrapper.constant.standard.Pkcs11ObjectClass.CKO_PRIVATE_KEY
import ru.rutoken.pkcs11wrapper.constant.standard.Pkcs11ObjectClass.CKO_PUBLIC_KEY
import ru.rutoken.pkcs11wrapper.datatype.Pkcs11KeyPair
import ru.rutoken.pkcs11wrapper.main.Pkcs11Session
import ru.rutoken.pkcs11wrapper.main.Pkcs11Token
import ru.rutoken.pkcs11wrapper.mechanism.Pkcs11Mechanism
import ru.rutoken.pkcs11wrapper.`object`.key.Pkcs11GostPrivateKeyObject
import ru.rutoken.pkcs11wrapper.`object`.key.Pkcs11GostPublicKeyObject
import ru.rutoken.tech.utils.loge

private const val CKA_ID_GROUP_SIZE = 8
private val CKA_ID_GROUP_CHARSET = ('a'..'f') + ('0'..'9')
private const val CKA_ID_GROUP_SEPARATOR = '-'

fun generateCkaId() = generateCkaIdGroup() + CKA_ID_GROUP_SEPARATOR.code.toByte() + generateCkaIdGroup()

/**
 * Method supposes that the user is logged in.
 */
fun Pkcs11Session.createGostKeyPair(
    keyPairParams: GostKeyPairParams,
    ckaId: ByteArray
): Pkcs11KeyPair<Pkcs11GostPublicKeyObject, Pkcs11GostPrivateKeyObject> {
    return tryGenerateObject(keyPairParams.mechanismType) {
        keyManager.generateKeyPair(
            Pkcs11GostPublicKeyObject::class.java,
            Pkcs11GostPrivateKeyObject::class.java,
            Pkcs11Mechanism.make(keyPairParams.mechanismType),
            attributeFactory.makeGostPublicKeyTemplate(keyPairParams, ckaId),
            attributeFactory.makeGostPrivateKeyTemplate(keyPairParams, ckaId)
        )
    }
}

private fun generateCkaIdGroup() = ByteArray(CKA_ID_GROUP_SIZE) { CKA_ID_GROUP_CHARSET.random().code.toByte() }

private fun <T> Pkcs11Session.tryGenerateObject(mechanismType: IPkcs11MechanismType, generateObjectBlock: () -> T): T {
    try {
        if (!token.isMechanismSupported(mechanismType))
            throw IllegalStateException("$mechanismType not supported by token")
        return generateObjectBlock()
    } catch (e: Exception) {
        loge { "Operation failed. ${e.message}" }
        throw RuntimeException(e)
    }
}

private fun Pkcs11Token.isMechanismSupported(mechanism: IPkcs11MechanismType) =
    mechanismList.any { it.asLong == mechanism.asLong }

private fun IPkcs11AttributeFactory.makeGostPublicKeyTemplate(
    keyPairParams: GostKeyPairParams,
    ckaId: ByteArray
): List<Pkcs11Attribute> {
    return listOf(
        makeAttribute(CKA_CLASS, CKO_PUBLIC_KEY),
        makeAttribute(CKA_KEY_TYPE, keyPairParams.keyType),
        makeAttribute(CKA_ID, ckaId),
        makeAttribute(CKA_PRIVATE, false),
        makeAttribute(CKA_GOSTR3410_PARAMS, keyPairParams.paramset3410),
        makeAttribute(CKA_GOSTR3411_PARAMS, keyPairParams.paramset3411),
        makeAttribute(CKA_TOKEN, true),
        makeAttribute(CKA_VERIFY, true),
        makeAttribute(CKA_ENCRYPT, true)
    )
}

private fun IPkcs11AttributeFactory.makeGostPrivateKeyTemplate(
    keyPairParams: GostKeyPairParams,
    ckaId: ByteArray
): List<Pkcs11Attribute> {
    return listOf(
        makeAttribute(CKA_CLASS, CKO_PRIVATE_KEY),
        makeAttribute(CKA_KEY_TYPE, keyPairParams.keyType),
        makeAttribute(CKA_ID, ckaId),
        makeAttribute(CKA_PRIVATE, true),
        makeAttribute(CKA_GOSTR3410_PARAMS, keyPairParams.paramset3410),
        makeAttribute(CKA_GOSTR3411_PARAMS, keyPairParams.paramset3411),
        makeAttribute(CKA_TOKEN, true),
        makeAttribute(CKA_SIGN, true),
        makeAttribute(CKA_DECRYPT, true)
    )
}
