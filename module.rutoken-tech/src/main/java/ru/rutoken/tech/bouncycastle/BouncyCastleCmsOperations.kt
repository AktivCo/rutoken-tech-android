package ru.rutoken.tech.bouncycastle

import org.bouncycastle.asn1.ASN1Encoding
import org.bouncycastle.cert.X509CertificateHolder
import org.bouncycastle.cert.jcajce.JcaCertStoreBuilder
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter
import org.bouncycastle.cms.CMSProcessableByteArray
import org.bouncycastle.cms.CMSSignedData
import org.bouncycastle.cms.CMSSignedDataGenerator
import org.bouncycastle.cms.SignerInfoGeneratorBuilder
import org.bouncycastle.cms.jcajce.JcaSimpleSignerInfoVerifierBuilder
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.util.Selector
import ru.rutoken.pkcs11wrapper.main.Pkcs11Session
import ru.rutoken.pkcs11wrapper.`object`.key.Pkcs11GostPrivateKeyObject
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
    fun signCmsDetached(
        session: Pkcs11Session,
        data: ByteArray,
        privateKey: Pkcs11GostPrivateKeyObject,
        certificate: X509CertificateHolder
    ): ByteArray {
        val signature =
            makeSignatureByHashOid(privateKey.getGostR3411ParamsAttributeValue(session).byteArrayValue, session)
        val signer = GostContentSigner(signature)
        signer.signInit(privateKey)
        val generator = CMSSignedDataGenerator().apply {
            addCertificate(certificate)
            addSignerInfoGenerator(SignerInfoGeneratorBuilder(signer.getDigestProvider()).build(signer, certificate))
        }

        return generator.generate(CMSProcessableByteArray(data)).getEncoded(ASN1Encoding.DER)
    }

    fun verifyDetachedCms(
        cms: ByteArray,
        data: ByteArray,
        trustedCertificates: List<X509Certificate>
    ): VerifyCmsResult {
        try {
            val cmsSignedData = CMSSignedData(CMSProcessableByteArray(data), cms)
            val trustAnchors = trustedCertificates.map { TrustAnchor(it, null) }.toHashSet()

            val certPathBuilder = CertPathBuilder.getInstance("PKIX", BouncyCastleProvider.PROVIDER_NAME)
            val cmsCertStore = cmsSignedData.certificates
            val signers = cmsSignedData.signerInfos

            for (signer in signers.signers) {
                val signerCertificates = cmsCertStore.getMatches(signer.sid as Selector<X509CertificateHolder>)
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

                    params.addCertStore(certStoreBuilder.build())
                    params.isRevocationEnabled = false
                    certPathBuilder.build(params)
                }
            }
        } catch (e: CertPathBuilderException) {
            loge(e) { "Certificate chain not verified. ${e.message}" }
            return VerifyCmsResult.CERTIFICATE_CHAIN_NOT_VERIFIED
        }

        return VerifyCmsResult.SUCCESS
    }
}
