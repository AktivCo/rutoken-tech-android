/*
 * Copyright (c) 2026, Aktiv-Soft JSC.
 * See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.tech.pkcs11.createobjects

import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.asn1.ASN1OctetString
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.DERBitString
import org.bouncycastle.asn1.DERNumericString
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers
import org.bouncycastle.asn1.x500.X500Name
import org.bouncycastle.asn1.x500.X500NameBuilder
import org.bouncycastle.asn1.x500.style.BCStyle
import org.bouncycastle.asn1.x509.AlgorithmIdentifier
import org.bouncycastle.asn1.x509.ExtendedKeyUsage
import org.bouncycastle.asn1.x509.Extension
import org.bouncycastle.asn1.x509.ExtensionsGenerator
import org.bouncycastle.asn1.x509.KeyPurposeId
import org.bouncycastle.asn1.x509.KeyUsage
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo
import org.bouncycastle.asn1.x9.X9ObjectIdentifiers
import org.bouncycastle.pkcs.PKCS10CertificationRequestBuilder
import ru.rutoken.pkcs11wrapper.constant.standard.Pkcs11MechanismType.CKM_ECDSA
import ru.rutoken.pkcs11wrapper.main.Pkcs11Session
import ru.rutoken.pkcs11wrapper.mechanism.Pkcs11Mechanism
import ru.rutoken.tech.bouncycastle.signature.EcdsaContentSigner


fun Pkcs11Session.createEcCsr(keyPair: EcKeyPair, dn: List<String>): ByteArray {
    val ecParams = keyPair.publicKey.getEcParamsAttributeValue(this).byteArrayValue
    val ecPoint = keyPair.publicKey.getEcPointAttributeValue(this).byteArrayValue

    return buildEcCsr(dn, ecParams, ecPoint) { hash ->
        signManager.signAtOnce(hash, Pkcs11Mechanism.make(CKM_ECDSA), keyPair.privateKey)
    }
}


fun buildEcCsr(
    dn: List<String>,
    ecParams: ByteArray,
    ecPoint: ByteArray,
    signHash: (ByteArray) -> ByteArray,
): ByteArray {
    val subjectPublicKeyInfo = SubjectPublicKeyInfo(
        AlgorithmIdentifier(X9ObjectIdentifiers.id_ecPublicKey, ASN1Primitive.fromByteArray(ecParams)),
        DERBitString(unwrapEcPoint(ecPoint))
    )

    val extensions = ExtensionsGenerator().apply {
        addExtension(
            Extension.keyUsage,
            false,
            KeyUsage(KeyUsage.digitalSignature or KeyUsage.nonRepudiation or KeyUsage.keyAgreement)
        )
        addExtension(
            Extension.extendedKeyUsage,
            false,
            ExtendedKeyUsage(arrayOf(KeyPurposeId.id_kp_clientAuth, KeyPurposeId.id_kp_emailProtection))
        )
    }.generate()

    return PKCS10CertificationRequestBuilder(dnToX500Name(dn), subjectPublicKeyInfo)
        .addAttribute(PKCSObjectIdentifiers.pkcs_9_at_extensionRequest, extensions)
        .build(EcdsaContentSigner(signHash))
        .encoded
}

private fun unwrapEcPoint(ecPoint: ByteArray): ByteArray =
    runCatching { ASN1OctetString.getInstance(ecPoint).octets }.getOrNull()
        ?.takeIf { it.size == ecPoint.size - 2 || it.size == ecPoint.size - 3 }
        ?: ecPoint

private val OGRN = ASN1ObjectIdentifier("1.2.643.100.1")
private val SNILS = ASN1ObjectIdentifier("1.2.643.100.3")
private val INNLE = ASN1ObjectIdentifier("1.2.643.100.4")

private val DN_TYPES = mapOf(
    "CN" to BCStyle.CN,
    "O" to BCStyle.O,
    "OU" to BCStyle.OU,
    "title" to BCStyle.T,
    "C" to BCStyle.C,
    "ST" to BCStyle.ST,
    "L" to BCStyle.L,
    "street" to BCStyle.STREET,
    "OGRN" to OGRN,
    "SNILS" to SNILS,
    "INNLE" to INNLE,
)

private fun dnToX500Name(dn: List<String>): X500Name {
    require(dn.size % 2 == 0) { "DN must consist of type-value pairs" }

    val builder = X500NameBuilder(BCStyle.INSTANCE)
    dn.chunked(2).forEach { (type, value) ->
        val oid = DN_TYPES[type] ?: ASN1ObjectIdentifier(type)
        if (oid == OGRN || oid == SNILS || oid == INNLE)
            builder.addRDN(oid, DERNumericString(value))
        else
            builder.addRDN(oid, value)
    }
    return builder.build()
}
