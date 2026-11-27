/*
 * Copyright (c) 2026, Aktiv-Soft JSC.
 * See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.tech.pkcs11.createobjects

import org.bouncycastle.asn1.ASN1Integer
import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.asn1.ASN1OctetString
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.DERNumericString
import org.bouncycastle.asn1.DERUTF8String
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers
import org.bouncycastle.asn1.x500.X500Name
import org.bouncycastle.asn1.x500.X500NameBuilder
import org.bouncycastle.asn1.x500.style.BCStyle
import org.bouncycastle.asn1.x509.AlgorithmIdentifier
import org.bouncycastle.asn1.x509.CertificatePolicies
import org.bouncycastle.asn1.x509.ExtendedKeyUsage
import org.bouncycastle.asn1.x509.Extension
import org.bouncycastle.asn1.x509.Extensions
import org.bouncycastle.asn1.x509.ExtensionsGenerator
import org.bouncycastle.asn1.x509.KeyPurposeId
import org.bouncycastle.asn1.x509.KeyUsage
import org.bouncycastle.asn1.x509.PolicyInformation
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo
import org.bouncycastle.asn1.x9.X9ObjectIdentifiers
import org.bouncycastle.operator.ContentSigner
import org.bouncycastle.pkcs.PKCS10CertificationRequestBuilder
import ru.rutoken.pkcs11wrapper.main.Pkcs11Session
import ru.rutoken.tech.bouncycastle.signature.EcdsaContentSigner
import ru.rutoken.tech.bouncycastle.signature.EcdsaSignature


fun Pkcs11Session.createEcCsr(
    keyPair: EcKeyPair,
    dn: List<String>,
    attributes: List<String>?,
    extensions: List<String>,
): ByteArray {
    val ecParams = keyPair.publicKey.getEcParamsAttributeValue(this).byteArrayValue
    val ecPoint = keyPair.publicKey.getEcPointAttributeValue(this).byteArrayValue

    val signature = EcdsaSignature(this, keyPair.privateKey)
    return buildEcCsr(dn, ecParams, ecPoint, attributes, extensions, EcdsaContentSigner(signature))
}


fun buildEcCsr(
    dn: List<String>,
    ecParams: ByteArray,
    ecPoint: ByteArray,
    attributes: List<String>?,
    extensions: List<String>,
    contentSigner: ContentSigner,
): ByteArray {
    val csrAttributes = attributes.orEmpty()
    require(csrAttributes.size % 2 == 0) { "Attributes must consist of name-value pairs" }

    val subjectPublicKeyInfo = SubjectPublicKeyInfo(
        AlgorithmIdentifier(
            X9ObjectIdentifiers.id_ecPublicKey,
            ASN1Primitive.fromByteArray(ecParams)
        ),
        unwrapEcPoint(ecPoint)
    )

    val builder = PKCS10CertificationRequestBuilder(dnToX500Name(dn), subjectPublicKeyInfo)

    for ((name, value) in csrAttributes.chunked(2)) {
        val oid = when (name) {
            "challengePassword" -> PKCSObjectIdentifiers.pkcs_9_at_challengePassword
            "unstructuredName" -> PKCSObjectIdentifiers.pkcs_9_at_unstructuredName
            else -> ASN1ObjectIdentifier(name)
        }

        require(oid != PKCSObjectIdentifiers.pkcs_9_at_extensionRequest) {
            "Pass requested extensions through the extensions parameter"
        }
        builder.addAttribute(oid, DERUTF8String(value))
    }

    if (extensions.isNotEmpty()) {
        builder.addAttribute(
            PKCSObjectIdentifiers.pkcs_9_at_extensionRequest,
            buildRequestedExtensions(extensions)
        )
    }

    return builder.build(contentSigner).encoded
}

private fun buildRequestedExtensions(extensions: List<String>): Extensions {
    require(extensions.size % 2 == 0) {
        "Extensions must consist of name-value pairs"
    }

    val generator = ExtensionsGenerator()

    for ((name, value) in extensions.chunked(2)) {
        when (name) {
            "keyUsage", Extension.keyUsage.id -> {
                var flags = 0
                for (usage in value.split(",")) {
                    val flag = when (usage.trim()) {
                        "digitalSignature" -> KeyUsage.digitalSignature
                        "nonRepudiation" -> KeyUsage.nonRepudiation
                        "keyEncipherment" -> KeyUsage.keyEncipherment
                        "dataEncipherment" -> KeyUsage.dataEncipherment
                        "keyAgreement" -> KeyUsage.keyAgreement
                        "keyCertSign" -> KeyUsage.keyCertSign
                        "cRLSign" -> KeyUsage.cRLSign
                        "encipherOnly" -> KeyUsage.encipherOnly
                        "decipherOnly" -> KeyUsage.decipherOnly
                        else -> throw IllegalArgumentException("Unsupported key usage: $usage")
                    }
                    flags = flags or flag
                }
                generator.addExtension(Extension.keyUsage, false, KeyUsage(flags))
            }

            "extendedKeyUsage", Extension.extendedKeyUsage.id -> {
                val purposes = value.split(",").map { oid ->
                    KeyPurposeId.getInstance(ASN1ObjectIdentifier(oid.trim()))
                }
                generator.addExtension(
                    Extension.extendedKeyUsage,
                    false,
                    ExtendedKeyUsage(purposes.toTypedArray())
                )
            }

            "certificatePolicies", Extension.certificatePolicies.id -> {
                val policies = value.split(",").map { oid ->
                    PolicyInformation(ASN1ObjectIdentifier(oid.trim()))
                }
                generator.addExtension(
                    Extension.certificatePolicies,
                    false,
                    CertificatePolicies(policies.toTypedArray())
                )
            }

            "subjectSignTool", "1.2.643.100.111" -> {
                val prefix = "ASN1:UTF8String:"
                require(value.startsWith(prefix)) { "Expected $prefix for subjectSignTool" }
                generator.addExtension(
                    ASN1ObjectIdentifier("1.2.643.100.111"),
                    false,
                    DERUTF8String(value.removePrefix(prefix))
                )
            }

            "1.2.643.100.114" -> {
                val prefix = "ASN1:INT:"
                require(value.startsWith(prefix)) { "Expected $prefix for $name" }
                generator.addExtension(
                    ASN1ObjectIdentifier(name),
                    false,
                    ASN1Integer(value.removePrefix(prefix).toLong())
                )
            }

            else -> throw IllegalArgumentException("Unsupported CSR extension: $name")
        }
    }

    return generator.generate()
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
