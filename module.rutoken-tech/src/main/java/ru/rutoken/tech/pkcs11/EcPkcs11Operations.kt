/*
 * Copyright (c) 2026, Aktiv-Soft JSC.
 * See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.tech.pkcs11

import ru.rutoken.pkcs11wrapper.attribute.IPkcs11AttributeFactory
import ru.rutoken.pkcs11wrapper.attribute.Pkcs11Attribute
import ru.rutoken.pkcs11wrapper.constant.standard.Pkcs11AttributeType.CKA_CLASS
import ru.rutoken.pkcs11wrapper.constant.standard.Pkcs11AttributeType.CKA_EXTRACTABLE
import ru.rutoken.pkcs11wrapper.constant.standard.Pkcs11AttributeType.CKA_KEY_TYPE
import ru.rutoken.pkcs11wrapper.constant.standard.Pkcs11AttributeType.CKA_SENSITIVE
import ru.rutoken.pkcs11wrapper.constant.standard.Pkcs11AttributeType.CKA_TOKEN
import ru.rutoken.pkcs11wrapper.constant.standard.Pkcs11AttributeType.CKA_VALUE
import ru.rutoken.pkcs11wrapper.constant.standard.Pkcs11AttributeType.CKA_VALUE_LEN
import ru.rutoken.pkcs11wrapper.constant.standard.Pkcs11KeyDerivationFunction.CKD_NULL
import ru.rutoken.pkcs11wrapper.constant.standard.Pkcs11KeyType.CKK_GENERIC_SECRET
import ru.rutoken.pkcs11wrapper.constant.standard.Pkcs11MechanismType.CKM_ECDH1_DERIVE
import ru.rutoken.pkcs11wrapper.constant.standard.Pkcs11ObjectClass.CKO_SECRET_KEY
import ru.rutoken.pkcs11wrapper.main.Pkcs11Session
import ru.rutoken.pkcs11wrapper.mechanism.Pkcs11Mechanism
import ru.rutoken.pkcs11wrapper.mechanism.parameter.CkEcdh1DeriveParams
import ru.rutoken.pkcs11wrapper.`object`.key.Pkcs11EcPrivateKeyObject
import ru.rutoken.pkcs11wrapper.`object`.key.Pkcs11SecretKeyObject

private const val SECP256R1_SHARED_SECRET_LENGTH = 32L

fun Pkcs11Session.deriveEcdhSharedSecret(privateKey: Pkcs11EcPrivateKeyObject, peerPublicPoint: ByteArray): ByteArray {
    val mechanism = Pkcs11Mechanism.make(CKM_ECDH1_DERIVE, CkEcdh1DeriveParams(CKD_NULL.asLong, peerPublicPoint, null))
    val secret = keyManager.deriveKey(
        Pkcs11SecretKeyObject::class.java,
        mechanism,
        privateKey,
        attributeFactory.makeEcdhSharedSecretTemplate()
    )

    try {
        return secret.getByteArrayAttributeValue(this, CKA_VALUE).byteArrayValue
    } finally {
        objectManager.destroyObject(secret)
    }
}

private fun IPkcs11AttributeFactory.makeEcdhSharedSecretTemplate(): List<Pkcs11Attribute> {
    return listOf(
        makeAttribute(CKA_CLASS, CKO_SECRET_KEY),
        makeAttribute(CKA_KEY_TYPE, CKK_GENERIC_SECRET),
        makeAttribute(CKA_TOKEN, false),
        makeAttribute(CKA_SENSITIVE, false),
        makeAttribute(CKA_EXTRACTABLE, true),
        makeAttribute(CKA_VALUE_LEN, SECP256R1_SHARED_SECRET_LENGTH)
    )
}