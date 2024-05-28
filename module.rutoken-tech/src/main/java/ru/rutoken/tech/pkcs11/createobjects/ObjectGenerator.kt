/*
 * Copyright (c) 2024, Aktiv-Soft JSC.
 * See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.tech.pkcs11.createobjects

import ru.rutoken.pkcs11wrapper.attribute.IPkcs11AttributeFactory
import ru.rutoken.pkcs11wrapper.attribute.Pkcs11Attribute
import ru.rutoken.pkcs11wrapper.constant.IPkcs11MechanismType
import ru.rutoken.pkcs11wrapper.constant.standard.Pkcs11AttributeType.CKA_CERTIFICATE_CATEGORY
import ru.rutoken.pkcs11wrapper.constant.standard.Pkcs11AttributeType.CKA_CERTIFICATE_TYPE
import ru.rutoken.pkcs11wrapper.constant.standard.Pkcs11AttributeType.CKA_CLASS
import ru.rutoken.pkcs11wrapper.constant.standard.Pkcs11AttributeType.CKA_DERIVE
import ru.rutoken.pkcs11wrapper.constant.standard.Pkcs11AttributeType.CKA_GOSTR3410_PARAMS
import ru.rutoken.pkcs11wrapper.constant.standard.Pkcs11AttributeType.CKA_GOSTR3411_PARAMS
import ru.rutoken.pkcs11wrapper.constant.standard.Pkcs11AttributeType.CKA_ID
import ru.rutoken.pkcs11wrapper.constant.standard.Pkcs11AttributeType.CKA_KEY_TYPE
import ru.rutoken.pkcs11wrapper.constant.standard.Pkcs11AttributeType.CKA_PRIVATE
import ru.rutoken.pkcs11wrapper.constant.standard.Pkcs11AttributeType.CKA_SIGN
import ru.rutoken.pkcs11wrapper.constant.standard.Pkcs11AttributeType.CKA_TOKEN
import ru.rutoken.pkcs11wrapper.constant.standard.Pkcs11AttributeType.CKA_VALUE
import ru.rutoken.pkcs11wrapper.constant.standard.Pkcs11AttributeType.CKA_VERIFY
import ru.rutoken.pkcs11wrapper.constant.standard.Pkcs11CertificateCategory.CK_CERTIFICATE_CATEGORY_TOKEN_USER
import ru.rutoken.pkcs11wrapper.constant.standard.Pkcs11CertificateType.CKC_X_509
import ru.rutoken.pkcs11wrapper.constant.standard.Pkcs11ObjectClass.CKO_CERTIFICATE
import ru.rutoken.pkcs11wrapper.constant.standard.Pkcs11ObjectClass.CKO_PRIVATE_KEY
import ru.rutoken.pkcs11wrapper.constant.standard.Pkcs11ObjectClass.CKO_PUBLIC_KEY
import ru.rutoken.pkcs11wrapper.datatype.Pkcs11KeyPair
import ru.rutoken.pkcs11wrapper.main.Pkcs11Session
import ru.rutoken.pkcs11wrapper.main.Pkcs11Token
import ru.rutoken.pkcs11wrapper.mechanism.Pkcs11Mechanism
import ru.rutoken.pkcs11wrapper.`object`.certificate.Pkcs11CertificateObject
import ru.rutoken.pkcs11wrapper.`object`.key.Pkcs11GostPrivateKeyObject
import ru.rutoken.pkcs11wrapper.`object`.key.Pkcs11GostPublicKeyObject
import ru.rutoken.pkcs11wrapper.rutoken.main.RtPkcs11Session
import ru.rutoken.tech.ca.LocalCA

typealias GostKeyPair = Pkcs11KeyPair<Pkcs11GostPublicKeyObject, Pkcs11GostPrivateKeyObject>

private const val CKA_ID_GROUP_SIZE = 8
private val CKA_ID_GROUP_CHARSET = ('a'..'f') + ('0'..'9')
private const val CKA_ID_GROUP_SEPARATOR = '-'

fun generateCkaId() = generateCkaIdGroup() + CKA_ID_GROUP_SEPARATOR.code.toByte() + generateCkaIdGroup()

/**
 * Method supposes that the user is logged in.
 */
fun RtPkcs11Session.createGostCertificate(
    keyPair: GostKeyPair,
    dn: List<String>,
    attributes: List<String>?,
    extensions: List<String>
): Pkcs11CertificateObject {
    val ckaId = keyPair.publicKey.getByteArrayAttributeValue(this, CKA_ID).byteArrayValue
    val csr = createCsr(keyPair.publicKey, dn, keyPair.privateKey, attributes, extensions)
    val encodedCertificate = LocalCA.issueCertificate(csr)

    return objectManager.createObject(
        Pkcs11CertificateObject::class.java,
        attributeFactory.makeCertificateTemplate(ckaId, encodedCertificate)
    )
}

/**
 * Method supposes that the user is logged in.
 */
fun Pkcs11Session.createGostKeyPair(keyPairParams: GostKeyPairParams, ckaId: ByteArray): GostKeyPair {
    if (!token.isMechanismSupported(keyPairParams.mechanismType))
        throw IllegalStateException("${keyPairParams.mechanismType} not supported by token")

    return keyManager.generateKeyPair(
        Pkcs11GostPublicKeyObject::class.java,
        Pkcs11GostPrivateKeyObject::class.java,
        Pkcs11Mechanism.make(keyPairParams.mechanismType),
        attributeFactory.makeGostPublicKeyTemplate(keyPairParams, ckaId),
        attributeFactory.makeGostPrivateKeyTemplate(keyPairParams, ckaId)
    )
}

fun Pkcs11Token.isMechanismSupported(mechanism: IPkcs11MechanismType) =
    mechanismList.any { it.asLong == mechanism.asLong }

fun IPkcs11AttributeFactory.makeCertificateTemplate(id: ByteArray, value: ByteArray): List<Pkcs11Attribute> {
    return listOf(
        makeAttribute(CKA_CLASS, CKO_CERTIFICATE),
        makeAttribute(CKA_CERTIFICATE_TYPE, CKC_X_509),
        makeAttribute(CKA_CERTIFICATE_CATEGORY, CK_CERTIFICATE_CATEGORY_TOKEN_USER.asLong),
        makeAttribute(CKA_ID, id),
        makeAttribute(CKA_TOKEN, true),
        makeAttribute(CKA_PRIVATE, false),
        makeAttribute(CKA_VALUE, value)
    )
}

fun IPkcs11AttributeFactory.makeGostPublicKeyTemplate(
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
    )
}

fun IPkcs11AttributeFactory.makeGostPrivateKeyTemplate(
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
        makeAttribute(CKA_DERIVE, true)
    )
}

private fun generateCkaIdGroup() = ByteArray(CKA_ID_GROUP_SIZE) { CKA_ID_GROUP_CHARSET.random().code.toByte() }
