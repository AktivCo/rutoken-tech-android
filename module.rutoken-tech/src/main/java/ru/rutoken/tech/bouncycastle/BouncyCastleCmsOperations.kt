/*
 * Copyright (c) 2024, Aktiv-Soft JSC.
 * See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.tech.bouncycastle

import org.bouncycastle.asn1.ASN1Encoding
import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.cert.X509CRLHolder
import org.bouncycastle.cert.X509CertificateHolder
import org.bouncycastle.cert.jcajce.JcaCertStoreBuilder
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter
import org.bouncycastle.cms.CMSAlgorithm
import org.bouncycastle.cms.CMSEnvelopedData
import org.bouncycastle.cms.CMSEnvelopedDataGenerator
import org.bouncycastle.cms.CMSException
import org.bouncycastle.cms.CMSProcessableByteArray
import org.bouncycastle.cms.CMSSignedData
import org.bouncycastle.cms.CMSSignedDataGenerator
import org.bouncycastle.cms.KeyTransRecipient
import org.bouncycastle.cms.RecipientInformationStore
import org.bouncycastle.cms.SignerInfoGeneratorBuilder
import org.bouncycastle.cms.jcajce.JcaSignerInfoGeneratorBuilder
import org.bouncycastle.cms.jcajce.JcaSimpleSignerInfoVerifierBuilder
import org.bouncycastle.cms.jcajce.JceCMSContentEncryptorBuilder
import org.bouncycastle.cms.jcajce.JceKeyTransRecipientId
import org.bouncycastle.cms.jcajce.JceKeyTransRecipientInfoGenerator
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder
import org.bouncycastle.util.CollectionStore
import org.bouncycastle.util.Selector
import ru.rutoken.pkcs11wrapper.main.Pkcs11Session
import ru.rutoken.pkcs11wrapper.`object`.key.Pkcs11GostPrivateKeyObject
import ru.rutoken.tech.bouncycastle.decrypt.RtGostKeyTransEnvelopedRecipient
import ru.rutoken.tech.bouncycastle.signature.GostContentSigner
import ru.rutoken.tech.bouncycastle.signature.makeSignatureByHashOid
import ru.rutoken.tech.pkcs11.Pkcs11CallScope.withPkcs11CallContext
import ru.rutoken.tech.ui.bank.payments.Base64String
import ru.rutoken.tech.utils.VerifyCmsResult
import ru.rutoken.tech.utils.base64ToPrivateKey
import ru.rutoken.tech.utils.loge
import java.security.cert.CertPathBuilder
import java.security.cert.CertPathBuilderException
import java.security.cert.PKIXBuilderParameters
import java.security.cert.TrustAnchor
import java.security.cert.X509CertSelector
import java.security.cert.X509Certificate

object BouncyCastleCmsOperations {

    /**
     * Generates or updates a CMS (Cryptographic Message Syntax) SignedData structure by adding a signatory.
     *
     * This function can operate in two modes:
     * 1. **Single signature** mode: If [existingCmsBytes] is `null`, it creates a new CMS SignedData structure and
     *    adds a single signatory (the one specified by [privateKey] and [certificate]).
     * 2. **Multiple signatures** mode: If [existingCmsBytes] is provided, the function appends the new signature to the
     *    existing SignedData structure, preserving any previously added signatories.
     *
     * Internally, this function uses PKCS#11 operations to sign the provided [data] with the specified GOST3410-2012-256
     * private key ([privateKey]) within the given [session]. The result is returned as a DER-encoded CMS SignedData [ByteArray].
     *
     * @param session A valid PKCS#11 session used to perform cryptographic operations (signing).
     * @param data The content to be signed.
     * @param privateKey The GOST3410-2012-256 private key used for signing.
     * @param certificate The primary X.509 certificate corresponding to the private key.
     * @param additionalCertificates A list of additional X.509 certificates to be included in the CMS structure.
     * @param existingCmsBytes An optional CMS SignedData byte array. If provided, the new signature is added
     *                         alongside any existing signatures; if `null`, a new CMS structure is created.
     *
     * @return A [ByteArray] containing the DER-encoded CMS SignedData with the passed signatory.
     */
    suspend fun signDetachedGost256Hardware(
        session: Pkcs11Session,
        data: ByteArray,
        privateKey: Pkcs11GostPrivateKeyObject,
        certificate: X509CertificateHolder,
        additionalCertificates: List<X509CertificateHolder> = emptyList(),
        existingCmsBytes: ByteArray? = null,
    ): ByteArray = withPkcs11CallContext {
        val generator = CMSSignedDataGenerator().apply {
            existingCmsBytes?.let {
                val cms = CMSSignedData(CMSProcessableByteArray(data), it)
                addCertificates(cms.certificates)
                addSigners(cms.signerInfos)
            }

            val signature =
                makeSignatureByHashOid(privateKey.getGostR3411ParamsAttributeValue(session).byteArrayValue, session)
            val contentSigner = GostContentSigner(signature).apply { signInit(privateKey) }

            addCertificate(certificate)
            additionalCertificates.forEach { addCertificate(it) }

            addSignerInfoGenerator(
                SignerInfoGeneratorBuilder(contentSigner.getDigestProvider()).build(contentSigner, certificate)
            )
        }

        generator.generate(CMSProcessableByteArray(data), false).getEncoded(ASN1Encoding.DER)
    }

    fun signDetachedGost256Software(
        data: ByteArray,
        privateKey: Base64String,
        certificate: X509CertificateHolder,
        additionalCertificates: List<X509CertificateHolder>,
    ): ByteArray {
        val generator = CMSSignedDataGenerator().apply {
            val signer = JcaContentSignerBuilder("GOST3411WITHECGOST3410-2012-256")
                .setProvider(BouncyCastleProvider.PROVIDER_NAME)
                .build(base64ToPrivateKey(privateKey, "ECGOST3410-2012"))
            val digestProvider =
                JcaDigestCalculatorProviderBuilder().setProvider(BouncyCastleProvider.PROVIDER_NAME).build()
            addCertificate(certificate)
            additionalCertificates.forEach { addCertificate(it) }
            addSignerInfoGenerator(JcaSignerInfoGeneratorBuilder(digestProvider).build(signer, certificate))
        }

        return generator.generate(CMSProcessableByteArray(data)).getEncoded(ASN1Encoding.DER)
    }

    fun verifyDetached(
        cms: ByteArray,
        data: ByteArray,
        trustedCertificates: List<X509Certificate>,
        intermediateCertificates: List<X509CertificateHolder>,
        additionalCertificates: List<X509CertificateHolder>,
        crls: List<X509CRLHolder>,
    ): VerifyCmsResult {
        try {
            val cmsSignedData = CMSSignedData(CMSProcessableByteArray(data), cms)
            val trustAnchors = trustedCertificates.map { TrustAnchor(it, null) }.toHashSet()

            val certPathBuilder = CertPathBuilder.getInstance("PKIX", BouncyCastleProvider.PROVIDER_NAME)
            val cmsCertStore = cmsSignedData.certificates
            val additionalCertsStore = CollectionStore(additionalCertificates)
            val signers = cmsSignedData.signerInfos

            for (signer in signers.signers) {
                val signerCertificates = cmsCertStore.getMatches(signer.sid as Selector<X509CertificateHolder>)
                if (signerCertificates.isEmpty()) {
                    val signersFromUser = additionalCertsStore.getMatches(signer.sid as Selector<X509CertificateHolder>)
                    if (signersFromUser.isEmpty())
                        throw RuntimeException("No signer certificate found")

                    signerCertificates.addAll(signersFromUser)
                }

                for (cert in signerCertificates) {
                    // Validate signer's signature
                    val verifier =
                        JcaSimpleSignerInfoVerifierBuilder().setProvider(BouncyCastleProvider.PROVIDER_NAME).build(cert)

                    try {
                        if (!signer.verify(verifier))
                            return VerifyCmsResult.SIGNATURE_INVALID
                    } catch (ex: CMSException) {
                        return VerifyCmsResult.SIGNATURE_INVALID
                    }

                    // Validate signer's certificate chain
                    val constraints = X509CertSelector().apply {
                        certificate = JcaX509CertificateConverter().setProvider(BouncyCastleProvider.PROVIDER_NAME)
                            .getCertificate(cert)
                    }

                    val certStoreBuilder = JcaCertStoreBuilder().apply {
                        addCertificates(cmsCertStore)
                        addCertificate(cert)
                        intermediateCertificates.forEach { addCertificate(it) }
                        crls.forEach { addCRL(it) }
                    }

                    val params = PKIXBuilderParameters(trustAnchors, constraints).apply {
                        addCertStore(certStoreBuilder.build())
                        isRevocationEnabled = crls.isNotEmpty()
                    }

                    /*
                     * According to the Oracle's docs, "all PKIX CertPathBuilders must return certification paths which
                     * have been validated according to the PKIX certification path validation algorithm."
                     */
                    certPathBuilder.build(params)
                }
            }
        } catch (e: CertPathBuilderException) {
            loge(e) { "Certificate chain not verified. ${e.message}" }
            return VerifyCmsResult.CERTIFICATE_CHAIN_NOT_VERIFIED
        }

        return VerifyCmsResult.SUCCESS
    }

    fun encrypt(
        data: ByteArray,
        certificateHolder: X509CertificateHolder,
        contentEncryptionAlgorithm: ASN1ObjectIdentifier,
    ): ByteArray {
        val cmsEnvelopedDataGenerator = CMSEnvelopedDataGenerator().apply {
            addRecipientInfoGenerator(JceKeyTransRecipientInfoGenerator(getX509Certificate(certificateHolder)))
        }
        val contentEncryptor = JceCMSContentEncryptorBuilder(contentEncryptionAlgorithm)
            .setProvider(BouncyCastleProvider.PROVIDER_NAME)
            .build()
        val cmsEnvelopedData = cmsEnvelopedDataGenerator.generate(CMSProcessableByteArray(data), contentEncryptor)

        return cmsEnvelopedData.encoded
    }

    suspend fun decrypt(
        session: Pkcs11Session,
        data: ByteArray,
        possibleRecipientsCertificates: List<X509CertificateHolder>,
        privateKey: Pkcs11GostPrivateKeyObject,
    ): ByteArray = withPkcs11CallContext {
        val cms = CMSEnvelopedData(data)
        val recipientsStore = cms.recipientInfos

        val matchedRecipients =
            possibleRecipientsCertificates.map { matchRecipients(recipientsStore, getX509Certificate(it)) }
                .firstOrNull { it.isNotEmpty() }
                ?: throw IllegalStateException("Certificates in $possibleRecipientsCertificates not found in CMS recipients")

        matchedRecipients.first().getContent(makeKeyTransEnvelopedRecipient(session, cms, privateKey))
    }
}

private fun getX509Certificate(certificateHolder: X509CertificateHolder): X509Certificate =
    JcaX509CertificateConverter().setProvider(BouncyCastleProvider.PROVIDER_NAME).getCertificate(certificateHolder)

private fun matchRecipients(recipientsStore: RecipientInformationStore, possibleRecipientCert: X509Certificate) =
    recipientsStore.getRecipients(JceKeyTransRecipientId(possibleRecipientCert))

private fun makeKeyTransEnvelopedRecipient(
    session: Pkcs11Session,
    cms: CMSEnvelopedData,
    privateKey: Pkcs11GostPrivateKeyObject,
): KeyTransRecipient {
    val encryptionAlgorithm = cms.contentEncryptionAlgorithm.algorithm
    if (encryptionAlgorithm != CMSAlgorithm.GOST28147_GCFB)
        throw IllegalArgumentException("Content encryption algorithm $encryptionAlgorithm isn't supported")

    return RtGostKeyTransEnvelopedRecipient(session, privateKey)
}
