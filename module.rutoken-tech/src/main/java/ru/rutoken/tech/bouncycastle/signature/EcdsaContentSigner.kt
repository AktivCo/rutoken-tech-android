/*
 * Copyright (c) 2026, Aktiv-Soft JSC.
 * See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.tech.bouncycastle.signature

import org.bouncycastle.asn1.ASN1Integer
import org.bouncycastle.asn1.DERSequence
import org.bouncycastle.asn1.x509.AlgorithmIdentifier
import org.bouncycastle.asn1.x9.X9ObjectIdentifiers
import org.bouncycastle.crypto.io.DigestOutputStream
import org.bouncycastle.operator.ContentSigner
import java.math.BigInteger

class EcdsaContentSigner(private val signature: EcdsaSignature) : ContentSigner {
    private val digestStream = DigestOutputStream(signature.makeDigest())

    override fun getAlgorithmIdentifier() = AlgorithmIdentifier(X9ObjectIdentifiers.ecdsa_with_SHA256)

    override fun getOutputStream() = digestStream

    override fun getSignature(): ByteArray {
        return rawEcdsaSignatureToDer(signature.sign(digestStream.digest))
    }
}

fun rawEcdsaSignatureToDer(rawSignature: ByteArray): ByteArray {
    require(rawSignature.isNotEmpty() && rawSignature.size % 2 == 0) {
        "Raw ECDSA signature has invalid length: ${rawSignature.size}"
    }
    val half = rawSignature.size / 2
    val r = BigInteger(1, rawSignature.copyOfRange(0, half))
    val s = BigInteger(1, rawSignature.copyOfRange(half, rawSignature.size))
    return DERSequence(arrayOf(ASN1Integer(r), ASN1Integer(s))).encoded
}
