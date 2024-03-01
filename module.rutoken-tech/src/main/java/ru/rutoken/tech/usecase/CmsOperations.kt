package ru.rutoken.tech.usecase

import org.bouncycastle.cert.X509CRLHolder
import org.bouncycastle.cert.X509CertificateHolder
import ru.rutoken.pkcs11wrapper.constant.standard.Pkcs11AttributeType.CKA_VALUE
import ru.rutoken.pkcs11wrapper.main.Pkcs11Session
import ru.rutoken.pkcs11wrapper.`object`.certificate.Pkcs11CertificateObject
import ru.rutoken.pkcs11wrapper.`object`.key.Pkcs11GostPrivateKeyObject
import ru.rutoken.pkcs11wrapper.rutoken.main.RtPkcs11Session
import ru.rutoken.tech.bouncycastle.BouncyCastleCmsOperations
import ru.rutoken.tech.pkcs11.Pkcs11WrapperCmsOperations
import ru.rutoken.tech.utils.VerifyCmsResult
import java.io.ByteArrayInputStream
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate

enum class CmsOperationProvider {
    PKCS11_WRAPPER,
    BOUNCY_CASTLE
}

object CmsOperations {
    /**
     * Method supposes that the user is logged in.
     */
    fun signDetached(
        provider: CmsOperationProvider,
        session: RtPkcs11Session,
        data: ByteArray,
        privateKey: Pkcs11GostPrivateKeyObject,
        certificate: Pkcs11CertificateObject,
        additionalCertificates: List<Pkcs11CertificateObject>? = null
    ): ByteArray {
        return when (provider) {
            CmsOperationProvider.PKCS11_WRAPPER -> Pkcs11WrapperCmsOperations.signDetached(
                session,
                data,
                privateKey,
                certificate,
                additionalCertificates
            )

            CmsOperationProvider.BOUNCY_CASTLE -> {
                val x509CertificateHolder = certificate.toX509CertificateHolder(session)
                val x509AdditionalCertificateHolders =
                    additionalCertificates.orEmpty().map { it.toX509CertificateHolder(session) }

                BouncyCastleCmsOperations.signDetached(
                    session,
                    data,
                    privateKey,
                    x509CertificateHolder,
                    x509AdditionalCertificateHolders
                )
            }
        }
    }

    /**
     * The [session] parameter is required only when verifying the signature via PKCS#11 wrapper.
     */
    fun verifyDetached(
        provider: CmsOperationProvider,
        cms: ByteArray,
        data: ByteArray,
        trustedCertificates: List<ByteArray>,
        session: RtPkcs11Session? = null,
        intermediateCertificates: List<ByteArray>? = null,
        additionalCertificates: List<ByteArray>? = null,
        crls: List<ByteArray>? = null
    ): VerifyCmsResult {
        return when (provider) {
            CmsOperationProvider.PKCS11_WRAPPER -> {
                if (session == null)
                    throw IllegalArgumentException("Session parameter cannot be null with ${provider.name}")

                Pkcs11WrapperCmsOperations.verifyDetached(
                    session,
                    cms,
                    data,
                    trustedCertificates,
                    intermediateCertificates,
                    crls
                )
            }

            CmsOperationProvider.BOUNCY_CASTLE -> {
                val x509TrustedCertificates = trustedCertificates.map {
                    CertificateFactory.getInstance("X.509")
                        .generateCertificate(ByteArrayInputStream(it)) as X509Certificate
                }
                val x509IntermediateCertificateHolders =
                    intermediateCertificates.orEmpty().map { X509CertificateHolder(it) }
                val x509AdditionalCertificateHolders =
                    additionalCertificates.orEmpty().map { X509CertificateHolder(it) }
                val x509Crls = crls.orEmpty().map { X509CRLHolder(it) }

                BouncyCastleCmsOperations.verifyDetached(
                    cms,
                    data,
                    x509TrustedCertificates,
                    x509IntermediateCertificateHolders,
                    x509AdditionalCertificateHolders,
                    x509Crls
                )
            }
        }
    }
}

private fun Pkcs11CertificateObject.toX509CertificateHolder(session: Pkcs11Session) =
    X509CertificateHolder(getByteArrayAttributeValue(session, CKA_VALUE).byteArrayValue)