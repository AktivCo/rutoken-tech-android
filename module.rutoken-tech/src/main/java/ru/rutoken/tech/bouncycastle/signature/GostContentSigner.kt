/*
 * Copyright (c) 2024, Aktiv-Soft JSC.
 * See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.tech.bouncycastle.signature

import org.bouncycastle.asn1.rosstandart.RosstandartObjectIdentifiers.id_tc26_signwithdigest_gost_3410_12_256
import org.bouncycastle.asn1.x509.AlgorithmIdentifier
import org.bouncycastle.crypto.io.DigestOutputStream
import org.bouncycastle.operator.ContentSigner
import ru.rutoken.pkcs11wrapper.`object`.key.Pkcs11GostPrivateKeyObject
import ru.rutoken.tech.bouncycastle.digest.DigestProvider
import ru.rutoken.tech.bouncycastle.digest.GostDigestCalculator

class GostContentSigner(private val signature: Signature) : ContentSigner {
    private val digestProvider: DigestProvider

    private val digestStream: DigestOutputStream

    init {
        val digest = signature.makeDigest()
        digestProvider = DigestProvider(GostDigestCalculator(digest))
        digestStream = DigestOutputStream(digest)
    }

    override fun getAlgorithmIdentifier(): AlgorithmIdentifier {
        return when (signature) {
            is Gost256Signature -> AlgorithmIdentifier(id_tc26_signwithdigest_gost_3410_12_256)
        }
    }

    override fun getOutputStream() = digestStream

    override fun getSignature() = signature.sign(digestProvider.digestCalculator.digest)

    fun getDigestProvider() = digestProvider

    fun signInit(privateKey: Pkcs11GostPrivateKeyObject) = signature.signInit(privateKey)
}
