/*
 * Copyright (c) 2026, Aktiv-Soft JSC.
 * See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.tech.bouncycastle.decrypt

import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.asn1.ASN1OctetString
import org.bouncycastle.asn1.cms.ecc.ECCCMSSharedInfo
import org.bouncycastle.asn1.nist.NISTObjectIdentifiers
import org.bouncycastle.asn1.x509.AlgorithmIdentifier
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo
import org.bouncycastle.asn1.x9.X9ObjectIdentifiers
import org.bouncycastle.cms.CMSAlgorithm
import org.bouncycastle.cms.KeyAgreeRecipient
import org.bouncycastle.cms.RecipientOperator
import org.bouncycastle.crypto.digests.SHA256Digest
import org.bouncycastle.crypto.engines.AESWrapEngine
import org.bouncycastle.crypto.generators.KDF2BytesGenerator
import org.bouncycastle.crypto.params.KDFParameters
import org.bouncycastle.crypto.params.KeyParameter
import org.bouncycastle.operator.InputDecryptor
import org.bouncycastle.util.Pack
import java.io.InputStream
import javax.crypto.Cipher
import javax.crypto.CipherInputStream
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class EcdhKeyAgreeEnvelopedRecipient(
    private val recipientKeyAlgorithm: AlgorithmIdentifier,
    private val deriveSharedSecret: (ByteArray) -> ByteArray,
) : KeyAgreeRecipient {
    override fun getPrivateKeyAlgorithmIdentifier() = recipientKeyAlgorithm

    override fun getRecipientOperator(
        keyEncryptionAlgorithm: AlgorithmIdentifier,
        contentEncryptionAlgorithm: AlgorithmIdentifier,
        senderPublicKey: SubjectPublicKeyInfo,
        userKeyingMaterial: ASN1OctetString?,
        encryptedContentKey: ByteArray,
    ): RecipientOperator {
        if (keyEncryptionAlgorithm.algorithm != CMSAlgorithm.ECDH_SHA256KDF)
            throw IllegalArgumentException("Key agreement algorithm ${keyEncryptionAlgorithm.algorithm} isn't supported")

        val wrapAlgorithm = AlgorithmIdentifier.getInstance(keyEncryptionAlgorithm.parameters)
        val kekSizeBits = aesWrapKeySizeBits(wrapAlgorithm.algorithm)

        val sharedSecret = deriveSharedSecret(senderPublicKey.publicKeyData.bytes)
        try {
            val sharedInfo = ECCCMSSharedInfo(
                wrapAlgorithm,
                userKeyingMaterial?.octets,
                Pack.intToBigEndian(kekSizeBits)
            ).encoded

            val kek = ByteArray(kekSizeBits / 8)
            KDF2BytesGenerator(SHA256Digest()).apply {
                init(KDFParameters(sharedSecret, sharedInfo))
                generateBytes(kek, 0, kek.size)
            }

            val contentKey = AESWrapEngine().run {
                init(false, KeyParameter(kek))
                unwrap(encryptedContentKey, 0, encryptedContentKey.size)
            }
            kek.fill(0)

            return RecipientOperator(AesCbcInputDecryptor(contentEncryptionAlgorithm, contentKey))
        } finally {
            sharedSecret.fill(0)
        }
    }

    private fun aesWrapKeySizeBits(oid: ASN1ObjectIdentifier) = when (oid) {
        NISTObjectIdentifiers.id_aes128_wrap -> 128
        NISTObjectIdentifiers.id_aes192_wrap -> 192
        NISTObjectIdentifiers.id_aes256_wrap -> 256
        else -> throw IllegalArgumentException("Key wrap algorithm $oid isn't supported")
    }

    companion object {
        fun recipientKeyAlgorithmOf(certificatePublicKey: SubjectPublicKeyInfo): AlgorithmIdentifier {
            require(certificatePublicKey.algorithm.algorithm == X9ObjectIdentifiers.id_ecPublicKey) {
                "Recipient certificate must contain an EC public key"
            }
            return certificatePublicKey.algorithm
        }
    }
}

class AesCbcInputDecryptor(
    private val algorithmIdentifier: AlgorithmIdentifier,
    private val key: ByteArray,
) : InputDecryptor {
    init {
        require(algorithmIdentifier.algorithm in AES_CBC_OIDS) {
            "Content encryption algorithm ${algorithmIdentifier.algorithm} isn't supported"
        }
    }

    override fun getAlgorithmIdentifier() = algorithmIdentifier

    override fun getInputStream(encryptedInput: InputStream): InputStream {
        val iv = ASN1OctetString.getInstance(algorithmIdentifier.parameters).octets
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding").apply {
            init(Cipher.DECRYPT_MODE, SecretKeySpec(key, "AES"), IvParameterSpec(iv))
        }
        return CipherInputStream(encryptedInput, cipher)
    }

    private companion object {
        val AES_CBC_OIDS = setOf(
            NISTObjectIdentifiers.id_aes128_CBC,
            NISTObjectIdentifiers.id_aes192_CBC,
            NISTObjectIdentifiers.id_aes256_CBC
        )
    }
}