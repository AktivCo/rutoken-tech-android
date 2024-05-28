/*
 * Copyright (c) 2024, Aktiv-Soft JSC.
 * See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.tech.bouncycastle.decrypt

import org.bouncycastle.asn1.ASN1OctetString
import org.bouncycastle.asn1.cryptopro.GOST28147Parameters
import org.bouncycastle.asn1.cryptopro.GostR3410KeyTransport
import org.bouncycastle.asn1.cryptopro.GostR3410TransportParameters
import org.bouncycastle.asn1.x509.AlgorithmIdentifier
import org.bouncycastle.cms.KeyTransRecipient
import org.bouncycastle.cms.RecipientOperator
import ru.rutoken.pkcs11wrapper.attribute.IPkcs11AttributeFactory
import ru.rutoken.pkcs11wrapper.attribute.Pkcs11Attribute
import ru.rutoken.pkcs11wrapper.constant.standard.Pkcs11AttributeType.CKA_CLASS
import ru.rutoken.pkcs11wrapper.constant.standard.Pkcs11AttributeType.CKA_EXTRACTABLE
import ru.rutoken.pkcs11wrapper.constant.standard.Pkcs11AttributeType.CKA_GOST28147_PARAMS
import ru.rutoken.pkcs11wrapper.constant.standard.Pkcs11AttributeType.CKA_KEY_TYPE
import ru.rutoken.pkcs11wrapper.constant.standard.Pkcs11AttributeType.CKA_SENSITIVE
import ru.rutoken.pkcs11wrapper.constant.standard.Pkcs11AttributeType.CKA_TOKEN
import ru.rutoken.pkcs11wrapper.constant.standard.Pkcs11KeyType.CKK_GOST28147
import ru.rutoken.pkcs11wrapper.constant.standard.Pkcs11MechanismType.CKM_GOST28147_KEY_WRAP
import ru.rutoken.pkcs11wrapper.constant.standard.Pkcs11ObjectClass.CKO_SECRET_KEY
import ru.rutoken.pkcs11wrapper.main.Pkcs11Session
import ru.rutoken.pkcs11wrapper.mechanism.Pkcs11Mechanism
import ru.rutoken.pkcs11wrapper.mechanism.parameter.CkGostR3410DeriveParams
import ru.rutoken.pkcs11wrapper.mechanism.parameter.Pkcs11ByteArrayMechanismParams
import ru.rutoken.pkcs11wrapper.`object`.key.Pkcs11GostPrivateKeyObject
import ru.rutoken.pkcs11wrapper.rutoken.constant.RtPkcs11MechanismType.CKM_GOSTR3410_12_DERIVE
import ru.rutoken.pkcs11wrapper.rutoken.constant.RtPkcs11MechanismType.CKM_KDF_4357

class RtGostKeyTransEnvelopedRecipient(
    private val session: Pkcs11Session,
    private val privateKey: Pkcs11GostPrivateKeyObject,
) : KeyTransRecipient {
    override fun getRecipientOperator(
        keyEncryptionAlgorithm: AlgorithmIdentifier,
        contentEncryptionAlgorithm: AlgorithmIdentifier,
        encryptedContentKey: ByteArray
    ): RecipientOperator {
        val transport = GostR3410KeyTransport.getInstance(encryptedContentKey)
        val transportParameters = transport.transportParameters
        val ukm = transportParameters.ukm

        return with(session) {
            // Derive to get KEK (Key Encryption Key)
            val ephemeralPublicKey =
                ASN1OctetString.getInstance(transportParameters.ephemeralPublicKey.parsePublicKey()).octets
            val deriveMechanismParameters = CkGostR3410DeriveParams(CKM_KDF_4357.asLong, ephemeralPublicKey, ukm)
            val deriveMechanism = Pkcs11Mechanism.make(CKM_GOSTR3410_12_DERIVE, deriveMechanismParameters)
            val derivedKey = keyManager.deriveKey(
                deriveMechanism,
                privateKey,
                attributeFactory.makeGost28147SecretKeyTemplate(transportParameters)
            )

            // Unwrap CEK (Content Encryption Key)
            val unwrapMechanism = Pkcs11Mechanism.make(CKM_GOST28147_KEY_WRAP, Pkcs11ByteArrayMechanismParams(ukm))
            val wrappedKey = transport.sessionEncryptedKey.encryptedKey + transport.sessionEncryptedKey.macKey
            val gost28147Parameters = GOST28147Parameters.getInstance(contentEncryptionAlgorithm.parameters)
            val unwrapedKey = keyManager.unwrapKey(
                unwrapMechanism,
                derivedKey,
                wrappedKey,
                attributeFactory.makeGost28147SessionKeyTemplate(gost28147Parameters)
            )

            RecipientOperator(RtInputDecryptor(session, unwrapedKey, contentEncryptionAlgorithm))
        }
    }
}

private fun IPkcs11AttributeFactory.makeGost28147SecretKeyTemplate(
    parameters: GostR3410TransportParameters
): List<Pkcs11Attribute> {
    return listOf(
        makeAttribute(CKA_CLASS, CKO_SECRET_KEY),
        makeAttribute(CKA_KEY_TYPE, CKK_GOST28147),
        makeAttribute(CKA_TOKEN, false),
        makeAttribute(CKA_GOST28147_PARAMS, parameters.encryptionParamSet.encoded)
    )
}

private fun IPkcs11AttributeFactory.makeGost28147SessionKeyTemplate(
    parameters: GOST28147Parameters
): List<Pkcs11Attribute> {
    return listOf(
        makeAttribute(CKA_CLASS, CKO_SECRET_KEY),
        makeAttribute(CKA_KEY_TYPE, CKK_GOST28147),
        makeAttribute(CKA_TOKEN, false),
        makeAttribute(CKA_SENSITIVE, false),
        makeAttribute(CKA_EXTRACTABLE, true),
        makeAttribute(CKA_GOST28147_PARAMS, parameters.encryptionParamSet.encoded)
    )
}
