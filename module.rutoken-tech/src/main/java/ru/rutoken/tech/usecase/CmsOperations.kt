package ru.rutoken.tech.usecase

import org.bouncycastle.cert.X509CertificateHolder
import ru.rutoken.pkcs11wrapper.constant.standard.Pkcs11AttributeType.CKA_VALUE
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

/**
 * Method supposes that the user is logged in.
 */
fun signDetachedGostCms(
    provider: CmsOperationProvider,
    session: RtPkcs11Session,
    data: ByteArray,
    privateKey: Pkcs11GostPrivateKeyObject,
    certificate: Pkcs11CertificateObject,
): ByteArray {
    return when (provider) {
        CmsOperationProvider.PKCS11_WRAPPER -> Pkcs11WrapperCmsOperations.signCmsDetached(
            session,
            data,
            privateKey,
            certificate
        )

        CmsOperationProvider.BOUNCY_CASTLE -> BouncyCastleCmsOperations.signCmsDetached(
            session,
            data,
            privateKey,
            X509CertificateHolder(certificate.getByteArrayAttributeValue(session, CKA_VALUE).byteArrayValue)
        )
    }
}

/**
 * The [session] parameter is required only when verifying the signature via PKCS#11 wrapper.
 */
fun verifyDetachedCms(
    provider: CmsOperationProvider,
    cms: ByteArray,
    data: ByteArray,
    trustedCertificates: List<ByteArray>,
    session: RtPkcs11Session? = null,
): VerifyCmsResult {
    return when (provider) {
        CmsOperationProvider.PKCS11_WRAPPER -> {
            if (session == null)
                throw IllegalArgumentException("Session parameter cannot be null with ${provider.name}")

            Pkcs11WrapperCmsOperations.verifyDetachedCms(session, cms, data, trustedCertificates)
        }

        CmsOperationProvider.BOUNCY_CASTLE -> {
            val x509TrustedCertificates = trustedCertificates.map {
                CertificateFactory.getInstance("X.509").generateCertificate(ByteArrayInputStream(it)) as X509Certificate
            }

            BouncyCastleCmsOperations.verifyDetachedCms(cms, data, x509TrustedCertificates)
        }
    }
}
