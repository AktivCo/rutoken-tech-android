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
import org.bouncycastle.cms.CMSProcessableByteArray
import org.bouncycastle.cms.CMSSignedData
import org.bouncycastle.cms.CMSSignedDataGenerator
import org.bouncycastle.cms.KeyTransRecipient
import org.bouncycastle.cms.RecipientInformationStore
import org.bouncycastle.cms.SignerInfoGeneratorBuilder
import org.bouncycastle.cms.jcajce.JcaSimpleSignerInfoVerifierBuilder
import org.bouncycastle.cms.jcajce.JceCMSContentEncryptorBuilder
import org.bouncycastle.cms.jcajce.JceKeyTransRecipientId
import org.bouncycastle.cms.jcajce.JceKeyTransRecipientInfoGenerator
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.util.CollectionStore
import org.bouncycastle.util.Selector
import ru.rutoken.pkcs11wrapper.main.Pkcs11Session
import ru.rutoken.pkcs11wrapper.`object`.key.Pkcs11GostPrivateKeyObject
import ru.rutoken.tech.bouncycastle.decrypt.RtGostKeyTransEnvelopedRecipient
import ru.rutoken.tech.bouncycastle.signature.GostContentSigner
import ru.rutoken.tech.bouncycastle.signature.makeSignatureByHashOid
import ru.rutoken.tech.utils.VerifyCmsResult
import ru.rutoken.tech.utils.loge
import java.security.cert.CertPathBuilder
import java.security.cert.CertPathBuilderException
import java.security.cert.PKIXBuilderParameters
import java.security.cert.TrustAnchor
import java.security.cert.X509CertSelector
import java.security.cert.X509Certificate

object BouncyCastleCmsOperations {
    fun signDetached(
        session: Pkcs11Session,
        data: ByteArray,
        privateKey: Pkcs11GostPrivateKeyObject,
        certificate: X509CertificateHolder,
        additionalCertificates: List<X509CertificateHolder>
    ): ByteArray {
        val signature =
            makeSignatureByHashOid(privateKey.getGostR3411ParamsAttributeValue(session).byteArrayValue, session)
        val signer = GostContentSigner(signature)
        signer.signInit(privateKey)
        val generator = CMSSignedDataGenerator().apply {
            addCertificate(certificate)
            additionalCertificates.forEach { addCertificate(it) }
            addSignerInfoGenerator(SignerInfoGeneratorBuilder(signer.getDigestProvider()).build(signer, certificate))
        }

        return generator.generate(CMSProcessableByteArray(data)).getEncoded(ASN1Encoding.DER)
    }

    fun verifyDetached(
        cms: ByteArray,
        data: ByteArray,
        trustedCertificates: List<X509Certificate>,
        intermediateCertificates: List<X509CertificateHolder>,
        additionalCertificates: List<X509CertificateHolder>,
        crls: List<X509CRLHolder>
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
                    if (!signer.verify(verifier))
                        return VerifyCmsResult.SIGNATURE_INVALID

                    // Validate signer's certificate chain
                    val constraints = X509CertSelector()
                    constraints.certificate =
                        JcaX509CertificateConverter().setProvider(BouncyCastleProvider.PROVIDER_NAME)
                            .getCertificate(cert)
                    val params = PKIXBuilderParameters(trustAnchors, constraints)

                    val certStoreBuilder = JcaCertStoreBuilder()
                    certStoreBuilder.addCertificate(cert)
                    intermediateCertificates.forEach { certStoreBuilder.addCertificate(it) }
                    crls.forEach { certStoreBuilder.addCRL(it) }

                    params.addCertStore(certStoreBuilder.build())
                    params.isRevocationEnabled = crls.isNotEmpty()
                    /* According to the Oracle's docs,
                     *      "all PKIX CertPathBuilders must return certification paths which have been
                     *      validated according to the PKIX certification path validation algorithm."
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
        contentEncryptionAlgorithm: ASN1ObjectIdentifier
    ): ByteArray {
        val cmsEnvelopedDataGenerator = CMSEnvelopedDataGenerator()
        val certificate = getX509Certificate(certificateHolder)
        cmsEnvelopedDataGenerator.addRecipientInfoGenerator(JceKeyTransRecipientInfoGenerator(certificate))
        val contentEncryptor = JceCMSContentEncryptorBuilder(contentEncryptionAlgorithm)
            .setProvider(BouncyCastleProvider.PROVIDER_NAME)
            .build()
        val cmsEnvelopedData = cmsEnvelopedDataGenerator.generate(CMSProcessableByteArray(data), contentEncryptor)

        return cmsEnvelopedData.encoded
    }

    fun decrypt(
        session: Pkcs11Session,
        data: ByteArray,
        possibleRecipientsCertificates: List<X509CertificateHolder>,
        privateKey: Pkcs11GostPrivateKeyObject
    ): ByteArray {
        val cms = CMSEnvelopedData(data)
        val recipientsStore = cms.recipientInfos

        val matchedRecipients =
            possibleRecipientsCertificates.map { matchRecipients(recipientsStore, getX509Certificate(it)) }
                .firstOrNull { it.isNotEmpty() }
                ?: throw IllegalStateException("Certificates in $possibleRecipientsCertificates not found in CMS recipients")

        return matchedRecipients.first().getContent(makeKeyTransEnvelopedRecipient(session, cms, privateKey))
    }
}

private fun getX509Certificate(certificateHolder: X509CertificateHolder): X509Certificate =
    JcaX509CertificateConverter().setProvider(BouncyCastleProvider.PROVIDER_NAME).getCertificate(certificateHolder)

private fun matchRecipients(recipientsStore: RecipientInformationStore, possibleRecipientCert: X509Certificate) =
    recipientsStore.getRecipients(JceKeyTransRecipientId(possibleRecipientCert))

private fun makeKeyTransEnvelopedRecipient(
    session: Pkcs11Session,
    cms: CMSEnvelopedData,
    privateKey: Pkcs11GostPrivateKeyObject
): KeyTransRecipient {
    val encryptionAlgorithm = cms.contentEncryptionAlgorithm.algorithm
    if (encryptionAlgorithm != CMSAlgorithm.GOST28147_GCFB)
        throw IllegalArgumentException("Content encryption algorithm $encryptionAlgorithm isn't supported")

    return RtGostKeyTransEnvelopedRecipient(session, privateKey)
}