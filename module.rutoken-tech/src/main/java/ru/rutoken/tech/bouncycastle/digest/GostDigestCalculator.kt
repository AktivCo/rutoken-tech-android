/*
 * Copyright (c) 2024, Aktiv-Soft JSC.
 * See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.tech.bouncycastle.digest

import org.bouncycastle.asn1.rosstandart.RosstandartObjectIdentifiers.id_tc26_gost_3411_12_256
import org.bouncycastle.asn1.x509.AlgorithmIdentifier
import org.bouncycastle.crypto.io.DigestOutputStream
import org.bouncycastle.operator.DigestCalculator

class GostDigestCalculator(private val digest: Pkcs11Digest) : DigestCalculator {
    private val digestStream = DigestOutputStream(digest)

    override fun getAlgorithmIdentifier(): AlgorithmIdentifier {
        return when (digest) {
            is Gost256Digest -> AlgorithmIdentifier(id_tc26_gost_3411_12_256)
        }
    }

    override fun getDigest() = ByteArray(digest.digestSize).also { digest.doFinal(it, 0) }

    override fun getOutputStream() = digestStream
}
