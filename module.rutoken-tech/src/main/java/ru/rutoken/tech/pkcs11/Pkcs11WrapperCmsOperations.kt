/*
 * Copyright (c) 2024, Aktiv-Soft JSC.
 * See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.tech.pkcs11

import ru.rutoken.pkcs11jna.RtPkcs11Constants.CKF_VENDOR_ALLOW_PARTIAL_CHAINS
import ru.rutoken.pkcs11jna.RtPkcs11Constants.PKCS7_DETACHED_SIGNATURE
import ru.rutoken.pkcs11wrapper.constant.standard.Pkcs11ReturnValue
import ru.rutoken.pkcs11wrapper.main.Pkcs11Exception
import ru.rutoken.pkcs11wrapper.`object`.certificate.Pkcs11CertificateObject
import ru.rutoken.pkcs11wrapper.`object`.key.Pkcs11GostPrivateKeyObject
import ru.rutoken.pkcs11wrapper.rutoken.constant.RtPkcs11ReturnValue
import ru.rutoken.pkcs11wrapper.rutoken.datatype.VendorX509Store
import ru.rutoken.pkcs11wrapper.rutoken.main.RtPkcs11Session
import ru.rutoken.pkcs11wrapper.rutoken.manager.RtPkcs11CmsManager.CrlCheckMode.OPTIONAL_CRL_CHECK
import ru.rutoken.tech.utils.VerifyCmsResult
import ru.rutoken.tech.utils.loge

object Pkcs11WrapperCmsOperations {
    fun signDetached(
        session: RtPkcs11Session,
        data: ByteArray,
        signerPrivateKey: Pkcs11GostPrivateKeyObject,
        signerCertificate: Pkcs11CertificateObject,
        additionalCertificates: List<Pkcs11CertificateObject>?
    ): ByteArray = session.cmsManager.sign(
        data,
        signerCertificate,
        signerPrivateKey,
        additionalCertificates,
        PKCS7_DETACHED_SIGNATURE
    )

    fun verifyDetached(
        session: RtPkcs11Session,
        cms: ByteArray,
        data: ByteArray,
        trustedCertificates: List<ByteArray>,
        certificates: List<ByteArray>?,
        crls: List<ByteArray>?
    ): VerifyCmsResult {
        val store = VendorX509Store(trustedCertificates, certificates, crls)
        val code = session.cmsManager.verifyDetachedAtOnce(
            cms,
            data,
            store,
            OPTIONAL_CRL_CHECK,
            CKF_VENDOR_ALLOW_PARTIAL_CHAINS
        ).result

        return when (code) {
            Pkcs11ReturnValue.CKR_OK -> VerifyCmsResult.SUCCESS
            Pkcs11ReturnValue.CKR_SIGNATURE_INVALID -> VerifyCmsResult.SIGNATURE_INVALID
            RtPkcs11ReturnValue.CKR_CERT_CHAIN_NOT_VERIFIED -> {
                loge { "Certificate chain not verified." }
                VerifyCmsResult.CERTIFICATE_CHAIN_NOT_VERIFIED
            }

            else -> {
                Pkcs11Exception.throwIfNotOk(code, "Signature verification failed.")
                // Unreachable. Pkcs11Exception#throwIfNotOk will always throw an exception
                return VerifyCmsResult.SIGNATURE_INVALID
            }
        }
    }
}
