/*
 * Copyright (c) 2026, Aktiv-Soft JSC.
 * See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.tech.rtpki

import com.sun.jna.Memory
import com.sun.jna.NativeLong
import com.sun.jna.ptr.NativeLongByReference
import com.sun.jna.ptr.PointerByReference
import org.bouncycastle.cert.X509CertificateHolder
import ru.rutoken.pkcs11wrapper.constant.IPkcs11ReturnValue
import ru.rutoken.pkcs11wrapper.main.Pkcs11Exception
import ru.rutoken.pkcs11wrapper.main.Pkcs11Session
import ru.rutoken.pkcs11wrapper.`object`.key.Pkcs11GostPrivateKeyObject
import ru.rutoken.pkcs11wrapper.`object`.key.Pkcs11GostPublicKeyObject
import ru.rutoken.rtpki.RtPkiData
import ru.rutoken.rtpki.RtPkiDataFormat.RT_PKI_DER
import ru.rutoken.rtpki.RtPkiDecryptParams
import ru.rutoken.rtpki.RtPkiEncryptParams
import ru.rutoken.rtpki.RtPkiResult
import ru.rutoken.tech.pkcs11.Pkcs11CallScope.withPkcs11CallContext
import ru.rutoken.tech.pkcs11.RtPkcs11Library
import ru.rutoken.tech.utils.loge

object RtPkiCmsOperations {
    /**
     * Encrypts data in CMS with Enveloped-data Content Type format.
     *
     * @param data data to be encrypted.
     * @param recipientsCertificates list of certificates that will be used to encrypt the data.
     * @param encryptionAlgorithm encryption algorithm.
     * @return CMS bytes in DER format.
     * @throws RtPkiException if encryption fails at any stage.
     * @see RtPkiEncryptionAlgorithm
     */
    @Suppress("UNCHECKED_CAST")
    fun encrypt(
        data: ByteArray,
        recipientsCertificates: List<X509CertificateHolder>,
        @RtPkiEncryptionAlgorithm encryptionAlgorithm: Int,
    ): ByteArray {
        val rtPki = RtPkiLibrary.INSTANCE
        var rv = rtPki.rt_pki_initialize()
        RtPkiException.throwIfNotOk("rt_pki_initialize", rv)

        try {
            val recipients =
                RtPkiData.ByReference().toArray(recipientsCertificates.size) as Array<RtPkiData.ByReference>
            for (i in recipientsCertificates.indices) {
                recipients[i].setData(RT_PKI_DER, recipientsCertificates[i].encoded)
            }

            val encryptParams = RtPkiEncryptParams(encryptionAlgorithm, recipients)
            val envelopedData = RtPkiData().apply {
                format = RT_PKI_DER
            }
            rv = rtPki.rt_pki_cms_encrypt(encryptParams, data, NativeLong(data.size.toLong()), envelopedData)
            RtPkiException.throwIfNotOk("rt_pki_cms_encrypt", rv)

            envelopedData.value = Memory(envelopedData.valueSize.toLong())
            rv = rtPki.rt_pki_cms_encrypt(encryptParams, data, NativeLong(data.size.toLong()), envelopedData)
            RtPkiException.throwIfNotOk("rt_pki_cms_encrypt", rv)

            return envelopedData.value.getByteArray(0, envelopedData.valueSize.toInt())
        } finally {
            rv = rtPki.rt_pki_finalize()
            if (rv != RtPkiResult.RT_PKI_OK) {
                loge { "rt_pki_finalize failed, error code = $rv" }
            }
        }
    }

    /**
     * Decrypts CMS with Enveloped-data Content Type using the provided PKCS#11 keys.
     *
     * @param session PKCS#11 session with logged-in user.
     * @param data CMS bytes in DER format to be decrypted.
     * @param publicKey GOST public key.
     * @param privateKey GOST private key.
     * @return Decrypted data as a bytes array.
     * @throws RtPkiException if rtpki operations fail.
     * @throws Pkcs11Exception if rtpkcs11ecp operations fail.
     */
    suspend fun decrypt(
        session: Pkcs11Session,
        data: ByteArray,
        publicKey: Pkcs11GostPublicKeyObject,
        privateKey: Pkcs11GostPrivateKeyObject,
    ): ByteArray = withPkcs11CallContext {
        val functionListPtr = PointerByReference()
        val rtPkcs11Rv = RtPkcs11Library.INSTANCE.C_GetFunctionList(functionListPtr)
        Pkcs11Exception.throwIfNotOk(IPkcs11ReturnValue.getInstance(rtPkcs11Rv.toLong()), "C_GetFunctionList")

        val rtPki = RtPkiLibrary.INSTANCE
        var rv = rtPki.rt_pki_initialize()
        RtPkiException.throwIfNotOk("rt_pki_initialize", rv)

        val rtPkiSessionPtr = PointerByReference()
        try {
            rv = rtPki.rt_pki_create_p11_session(
                rtPkiSessionPtr,
                functionListPtr.value,
                NativeLong(session.sessionHandle)
            )
            RtPkiException.throwIfNotOk("rt_pki_create_p11_session", rv)

            val decryptParams = RtPkiDecryptParams(
                rtPkiSessionPtr.value,
                NativeLong(publicKey.handle),
                NativeLong(privateKey.handle)
            )
            val decryptedDataSize = NativeLongByReference()
            val envelopedData = RtPkiData(RT_PKI_DER, data)
            rv = rtPki.rt_pki_cms_decrypt(decryptParams, envelopedData, null, decryptedDataSize)
            RtPkiException.throwIfNotOk("rt_pki_cms_decrypt", rv)

            val decryptedData = ByteArray(decryptedDataSize.value.toInt())
            rv = rtPki.rt_pki_cms_decrypt(decryptParams, envelopedData, decryptedData, decryptedDataSize)
            RtPkiException.throwIfNotOk("rt_pki_cms_decrypt", rv)

            decryptedData
        } finally {
            rv = rtPki.rt_pki_destroy_p11_session(rtPkiSessionPtr.value)
            if (rv != RtPkiResult.RT_PKI_OK) {
                loge<RtPkiCmsOperations> { "rt_pki_destroy_p11_session failed, error code = $rv" }
            }

            rv = rtPki.rt_pki_finalize()
            if (rv != RtPkiResult.RT_PKI_OK) {
                loge<RtPkiCmsOperations> { "rt_pki_finalize failed, error code = $rv" }
            }
        }
    }
}
