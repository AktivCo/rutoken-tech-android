/*
 * Copyright (c) 2026, Aktiv-Soft JSC.
 * See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.tech.bouncycastle

import org.bouncycastle.asn1.ASN1Encoding
import org.bouncycastle.cert.X509CertificateHolder
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter
import org.bouncycastle.cms.CMSAlgorithm
import org.bouncycastle.cms.CMSEnvelopedData
import org.bouncycastle.cms.CMSEnvelopedDataGenerator
import org.bouncycastle.cms.CMSProcessableByteArray
import org.bouncycastle.cms.CMSSignedDataGenerator
import org.bouncycastle.cms.SignerInfoGeneratorBuilder
import org.bouncycastle.cms.jcajce.JceCMSContentEncryptorBuilder
import org.bouncycastle.cms.jcajce.JceKeyAgreeRecipientId
import org.bouncycastle.cms.jcajce.JceKeyAgreeRecipientInfoGenerator
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.operator.ContentSigner
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder
import ru.rutoken.tech.bouncycastle.decrypt.EcdhKeyAgreeEnvelopedRecipient
import ru.rutoken.tech.pkcs11.createobjects.EcKeyPairParams
import ru.rutoken.tech.ui.bank.payments.Base64String
import ru.rutoken.tech.utils.base64ToPrivateKey
import java.security.KeyPairGenerator
import java.security.spec.ECGenParameterSpec

object EcCmsOperations {
    fun signDetached(
        data: ByteArray,
        contentSigner: ContentSigner,
        certificate: X509CertificateHolder,
        additionalCertificates: List<X509CertificateHolder> = emptyList(),
    ): ByteArray {
        val digestProvider = JcaDigestCalculatorProviderBuilder().setProvider(BouncyCastleProvider.PROVIDER_NAME).build()
        val generator = CMSSignedDataGenerator().apply {
            addCertificate(certificate)
            additionalCertificates.forEach { addCertificate(it) }
            addSignerInfoGenerator(SignerInfoGeneratorBuilder(digestProvider).build(contentSigner, certificate))
        }

        return generator.generate(CMSProcessableByteArray(data), false).getEncoded(ASN1Encoding.DER)
    }

    fun signDetachedSoftware(
        data: ByteArray,
        privateKey: Base64String,
        certificate: X509CertificateHolder,
        additionalCertificates: List<X509CertificateHolder>,
    ): ByteArray {
        val signer = JcaContentSignerBuilder("SHA256withECDSA")
            .setProvider(BouncyCastleProvider.PROVIDER_NAME)
            .build(base64ToPrivateKey(privateKey, "EC"))

        return signDetached(data, signer, certificate, additionalCertificates)
    }

    fun encrypt(data: ByteArray, recipientCertificate: X509CertificateHolder): ByteArray {
        val certificate = JcaX509CertificateConverter().setProvider(BouncyCastleProvider.PROVIDER_NAME)
            .getCertificate(recipientCertificate)

        val ephemeralKeyPair = KeyPairGenerator.getInstance("EC", BouncyCastleProvider.PROVIDER_NAME).run {
            initialize(ECGenParameterSpec(EcKeyPairParams.SECP256R1.curveName))
            generateKeyPair()
        }

        val recipientInfoGenerator = JceKeyAgreeRecipientInfoGenerator(
            CMSAlgorithm.ECDH_SHA256KDF,
            ephemeralKeyPair.private,
            ephemeralKeyPair.public,
            CMSAlgorithm.AES256_WRAP
        ).setProvider(BouncyCastleProvider.PROVIDER_NAME).addRecipient(certificate)

        val contentEncryptor = JceCMSContentEncryptorBuilder(CMSAlgorithm.AES256_CBC)
            .setProvider(BouncyCastleProvider.PROVIDER_NAME)
            .build()

        return CMSEnvelopedDataGenerator().apply { addRecipientInfoGenerator(recipientInfoGenerator) }
            .generate(CMSProcessableByteArray(data), contentEncryptor)
            .encoded
    }

    fun decrypt(
        data: ByteArray,
        recipientCertificate: X509CertificateHolder,
        deriveSharedSecret: (ByteArray) -> ByteArray,
    ): ByteArray {
        val certificate = JcaX509CertificateConverter().setProvider(BouncyCastleProvider.PROVIDER_NAME)
            .getCertificate(recipientCertificate)
        val recipientInformation = CMSEnvelopedData(data).recipientInfos.getRecipients(JceKeyAgreeRecipientId(certificate))
            .firstOrNull() ?: throw IllegalStateException("Certificate is not found in CMS recipients")

        val recipient = EcdhKeyAgreeEnvelopedRecipient(
            EcdhKeyAgreeEnvelopedRecipient.recipientKeyAlgorithmOf(recipientCertificate.subjectPublicKeyInfo),
            deriveSharedSecret
        )
        return recipientInformation.getContent(recipient)
    }
}
