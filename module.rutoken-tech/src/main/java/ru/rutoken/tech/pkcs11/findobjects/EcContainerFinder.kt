/*
 * Copyright (c) 2026, Aktiv-Soft JSC.
 * See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.tech.pkcs11.findobjects

import ru.rutoken.pkcs11wrapper.attribute.Pkcs11ByteArrayAttribute
import ru.rutoken.pkcs11wrapper.constant.standard.Pkcs11AttributeType.CKA_EC_PARAMS
import ru.rutoken.pkcs11wrapper.constant.standard.Pkcs11AttributeType.CKA_ID
import ru.rutoken.pkcs11wrapper.datatype.Pkcs11KeyPair
import ru.rutoken.pkcs11wrapper.main.Pkcs11Session
import ru.rutoken.pkcs11wrapper.`object`.certificate.Pkcs11X509PublicKeyCertificateObject
import ru.rutoken.pkcs11wrapper.`object`.key.Pkcs11EcPrivateKeyObject
import ru.rutoken.pkcs11wrapper.`object`.key.Pkcs11EcPublicKeyObject
import ru.rutoken.tech.pkcs11.Pkcs11CallScope.withPkcs11CallContext
import ru.rutoken.tech.pkcs11.createobjects.EcKeyPair
import ru.rutoken.tech.pkcs11.createobjects.EcKeyPairParams
import android.util.Base64
import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.asn1.x9.X9ObjectIdentifiers
import org.bouncycastle.cert.X509CertificateHolder
import ru.rutoken.tech.utils.loge

suspend fun Pkcs11Session.findEcKeyPairByCkaId(
    ckaId: ByteArray,
    keyPairParams: EcKeyPairParams = EcKeyPairParams.SECP256R1,
): EcKeyPair = withPkcs11CallContext {
    val publicKeyTemplate = listOf(
        Pkcs11ByteArrayAttribute(CKA_ID, ckaId),
        Pkcs11ByteArrayAttribute(CKA_EC_PARAMS, keyPairParams.ecParams)
    )
    val publicKey =
        objectManager.findObjectsAtOnce(Pkcs11EcPublicKeyObject::class.java, publicKeyTemplate)
            .singleOrThrow()

    val privateKeyTemplate = listOf(Pkcs11ByteArrayAttribute(CKA_ID, ckaId))
    val privateKey =
        objectManager.findObjectsAtOnce(Pkcs11EcPrivateKeyObject::class.java, privateKeyTemplate)
            .singleOrThrow()

    Pkcs11KeyPair(publicKey, privateKey)
}

suspend fun Pkcs11Session.findEcKeyPairsWithoutCertificate(
    keyPairParams: EcKeyPairParams = EcKeyPairParams.SECP256R1,
): List<ByteArray> = withPkcs11CallContext {
    val certificateIds =
        objectManager.findObjectsAtOnce(Pkcs11X509PublicKeyCertificateObject::class.java)
            .mapNotNull { runCatching { it.getIdAttributeValue(this@findEcKeyPairsWithoutCertificate).byteArrayValue }.getOrNull() }

    val publicKeys = objectManager.findObjectsAtOnce(
        Pkcs11EcPublicKeyObject::class.java,
        listOf(Pkcs11ByteArrayAttribute(CKA_EC_PARAMS, keyPairParams.ecParams))
    )

    publicKeys.mapNotNull { publicKey ->
        val ckaId = runCatching {
            publicKey.getIdAttributeValue(this@findEcKeyPairsWithoutCertificate).byteArrayValue
        }.getOrNull() ?: return@mapNotNull null

        if (certificateIds.any { it.contentEquals(ckaId) }) return@mapNotNull null

        val hasPrivateKey = objectManager.findObjectsAtOnce(
            Pkcs11EcPrivateKeyObject::class.java,
            listOf(Pkcs11ByteArrayAttribute(CKA_ID, ckaId))
        ).size == 1

        if (hasPrivateKey) ckaId else null
    }
}

suspend fun Pkcs11Session.findEcCertificateAndKeyContainers(
    keyPairParams: EcKeyPairParams = EcKeyPairParams.SECP256R1,
): List<EcCertificateAndKeyContainer> = withPkcs11CallContext {
    val curveOid = ASN1ObjectIdentifier.getInstance(keyPairParams.ecParams)

    objectManager.findObjectsAtOnce(Pkcs11X509PublicKeyCertificateObject::class.java).mapNotNull { certificate ->
        val certificateValue = certificate.getValueAttributeValue(this@findEcCertificateAndKeyContainers).byteArrayValue
        val x509CertificateHolder = try {
            X509CertificateHolder(certificateValue)
        } catch (e: Exception) {
            loge<Container>(e) { "Unable to parse certificate" }
            loge<Container> { "Unparsed certificate(base64-encoded): ${Base64.encodeToString(certificateValue, Base64.DEFAULT)}" }
            return@mapNotNull null
        }

        val algorithm = x509CertificateHolder.subjectPublicKeyInfo.algorithm
        if (algorithm.algorithm != X9ObjectIdentifiers.id_ecPublicKey || algorithm.parameters != curveOid)
            return@mapNotNull null

        val ckaId = runCatching {
            certificate.getIdAttributeValue(this@findEcCertificateAndKeyContainers).byteArrayValue
        }.getOrNull() ?: return@mapNotNull null

        val keyPair = try {
            findEcKeyPairByCkaId(ckaId, keyPairParams)
        } catch (_: IllegalStateException) {
            return@mapNotNull null
        }

        EcCertificateAndKeyContainer(ckaId, x509CertificateHolder, keyPair)
    }
}

suspend fun Pkcs11Session.findEcCertificateAndKeyContainerByCkaId(ckaId: ByteArray) =
    findEcCertificateAndKeyContainers().filter { it.ckaId.contentEquals(ckaId) }.singleOrThrow()