package ru.rutoken.tech.usecase

import org.bouncycastle.cert.X509CertificateHolder
import ru.rutoken.pkcs11wrapper.constant.standard.Pkcs11AttributeType.CKA_VALUE
import ru.rutoken.pkcs11wrapper.`object`.certificate.Pkcs11CertificateObject
import ru.rutoken.pkcs11wrapper.`object`.key.Pkcs11GostPrivateKeyObject
import ru.rutoken.pkcs11wrapper.rutoken.main.RtPkcs11Session
import ru.rutoken.tech.bouncycastle.signCmsDetachedWithBouncyCastle
import ru.rutoken.tech.pkcs11.signCmsDetachedWithPkcs11Wrapper

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
        CmsOperationProvider.PKCS11_WRAPPER -> session.signCmsDetachedWithPkcs11Wrapper(data, privateKey, certificate)
        CmsOperationProvider.BOUNCY_CASTLE -> {
            val x509CertificateHolder =
                X509CertificateHolder(certificate.getByteArrayAttributeValue(session, CKA_VALUE).byteArrayValue)
            signCmsDetachedWithBouncyCastle(session, data, privateKey, x509CertificateHolder)
        }
    }
}
