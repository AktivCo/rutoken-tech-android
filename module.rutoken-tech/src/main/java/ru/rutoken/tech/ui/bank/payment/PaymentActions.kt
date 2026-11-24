/*
 * Copyright (c) 2024, Aktiv-Soft JSC.
 * See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.tech.ui.bank.payment

import android.content.Context
import androidx.annotation.WorkerThread
import org.bouncycastle.cert.X509CertificateHolder
import ru.rutoken.pkcs11wrapper.main.Pkcs11Session
import ru.rutoken.pkcs11wrapper.rutoken.main.RtPkcs11Session
import ru.rutoken.tech.bank.BANK_CERTIFICATE_GOST
import ru.rutoken.tech.ca.LocalCA
import ru.rutoken.tech.pkcs11.findobjects.findGost256CertificateByCkaId
import ru.rutoken.tech.pkcs11.findobjects.findGost256KeyPairByCkaId
import ru.rutoken.tech.rtpki.RtPkiCmsOperations
import ru.rutoken.tech.session.BankEncryptionAlgorithm
import ru.rutoken.tech.ui.bank.payments.Payment
import ru.rutoken.tech.usecase.CmsOperationProvider
import ru.rutoken.tech.usecase.CmsOperations
import ru.rutoken.tech.utils.VerifyCmsResult
import ru.rutoken.tech.utils.base64ToX509CertificateHolder
import ru.rutoken.tech.utils.decoded
import ru.rutoken.tech.utils.toBase64String
import java.time.LocalDateTime
import ru.rutoken.tech.bank.BANK_CERTIFICATE_ECDSA
import ru.rutoken.tech.bouncycastle.EcCmsOperations
import ru.rutoken.tech.bouncycastle.signature.EcdsaContentSigner
import ru.rutoken.tech.bouncycastle.signature.EcdsaSignature
import ru.rutoken.tech.pkcs11.Pkcs11CallScope.withPkcs11CallContext
import ru.rutoken.tech.pkcs11.deriveEcdhSharedSecret
import ru.rutoken.tech.pkcs11.findobjects.findEcCertificateAndKeyContainerByCkaId
import ru.rutoken.tech.pkcs11.findobjects.findEcKeyPairByCkaId
import ru.rutoken.tech.utils.isEcCertificate

suspend fun RtPkcs11Session.signPayment(
    payment: Payment,
    certificateCkaId: ByteArray,
    certificate: ByteArray,
    applicationContext: Context,
    provider: CmsOperationProvider = CmsOperationProvider.PKCS11_WRAPPER,
) {
    if (isEcCertificate(certificate)) {
        signPaymentEcdsa(payment, certificateCkaId, applicationContext)
        return
    }

    payment.apply {
        actionResultData = CmsOperations.signDetached(
            provider,
            this@signPayment,
            getActionData(applicationContext).decoded,
            findGost256KeyPairByCkaId(certificateCkaId).privateKey,
            findGost256CertificateByCkaId(certificateCkaId),
            listOf(X509CertificateHolder(LocalCA.caCertificateGost))
        ).toBase64String()

        actionTime = LocalDateTime.now()
    }
}

suspend fun verifyPaymentSignature(
    payment: Payment,
    applicationContext: Context,
    provider: CmsOperationProvider = CmsOperationProvider.PKCS11_WRAPPER,
    session: RtPkcs11Session? = null,
): VerifyCmsResult {
    payment.apply {
        val verifyResult = CmsOperations.verifyDetached(
            provider = provider,
            cms = getActionData(applicationContext).decoded,
            data = getRenderData(applicationContext).decoded,
            trustedCertificates = when (provider) {
                CmsOperationProvider.BOUNCY_CASTLE -> listOf(LocalCA.rootCertificateGost, LocalCA.rootCertificateEcdsa)
                CmsOperationProvider.PKCS11_WRAPPER -> listOf(LocalCA.rootCertificateGost)
            },
            session = session
        )

        actionTime = LocalDateTime.now()

        return verifyResult
    }
}

@WorkerThread
fun encryptPayment(
    payment: Payment,
    encryptionAlgorithm: BankEncryptionAlgorithm,
    userCertificate: ByteArray,
    applicationContext: Context
) {
    if (isEcCertificate(userCertificate)) {
        payment.apply {
            actionResultData = EcCmsOperations.encrypt(
                readFile(applicationContext),
                base64ToX509CertificateHolder(BANK_CERTIFICATE_ECDSA)
            ).toBase64String()

            actionTime = LocalDateTime.now()
        }
        return
    }
    payment.apply {
        /*
         * BouncyCastleCmsOperations#encrypt method with the GOST28147_GCFB encryption algorithm can be used if
         * the GOST 28147-89 encryption algorithm is used throughout the application:
         *
         * actionResultData = BouncyCastleCmsOperations.encrypt(
         *     readFile(applicationContext),
         *     base64ToX509CertificateHolder(BANK_CERTIFICATE_GOST),
         *     GOST28147_GCFB
         * ).toBase64String()
         */
        actionResultData = RtPkiCmsOperations.encrypt(
            readFile(applicationContext),
            listOf(base64ToX509CertificateHolder(BANK_CERTIFICATE_GOST)),
            encryptionAlgorithm.toRtPkiEncryptionAlgorithm()
        ).toBase64String()

        actionTime = LocalDateTime.now()
    }
}

suspend fun Pkcs11Session.decryptPayment(
    payment: Payment,
    certificateCkaId: ByteArray,
    certificateBytes: ByteArray,
    applicationContext: Context,
) {
    if (isEcCertificate(certificateBytes)) {
        decryptPaymentEcdh(payment, certificateCkaId, certificateBytes, applicationContext)
        return
    }
    payment.apply {
        val keyPair = findGost256KeyPairByCkaId(certificateCkaId)
        /*
         * BouncyCastleCmsOperations#decrypt method can be used if the GOST 28147-89 encryption algorithm is used
         * throughout the application:
         *
         * actionResultData = BouncyCastleCmsOperations.decrypt(
         *     session = this@decryptPayment,
         *     data = getActionData(applicationContext).decoded,
         *     possibleRecipientsCertificates = listOf(X509CertificateHolder(certificateBytes)),
         *     privateKey = findGost256KeyPairByCkaId(certificateCkaId).privateKey
         * ).toBase64String()
         */
        actionResultData = RtPkiCmsOperations.decrypt(
            session = this@decryptPayment,
            data = getActionData(applicationContext).decoded,
            publicKey = keyPair.publicKey,
            privateKey = keyPair.privateKey
        ).toBase64String()

        actionTime = LocalDateTime.now()
    }
}

private suspend fun RtPkcs11Session.signPaymentEcdsa(
    payment: Payment,
    certificateCkaId: ByteArray,
    applicationContext: Context,
) = withPkcs11CallContext {
    val container = findEcCertificateAndKeyContainerByCkaId(certificateCkaId)
    val signature = EcdsaSignature(this@signPaymentEcdsa, container.keyPair.privateKey)
    val contentSigner = EcdsaContentSigner(signature)

    payment.apply {
        actionResultData = EcCmsOperations.signDetached(
            getActionData(applicationContext).decoded,
            contentSigner,
            container.certificate,
            listOf(X509CertificateHolder(LocalCA.caCertificateEcdsa))
        ).toBase64String()

        actionTime = LocalDateTime.now()
    }
}

private suspend fun Pkcs11Session.decryptPaymentEcdh(
    payment: Payment,
    certificateCkaId: ByteArray,
    certificateBytes: ByteArray,
    applicationContext: Context,
) = withPkcs11CallContext {
    val privateKey = findEcKeyPairByCkaId(certificateCkaId).privateKey

    payment.apply {
        actionResultData = EcCmsOperations.decrypt(
            getActionData(applicationContext).decoded,
            X509CertificateHolder(certificateBytes)
        ) { peerPublicPoint ->
            deriveEcdhSharedSecret(privateKey, peerPublicPoint)
        }.toBase64String()

        actionTime = LocalDateTime.now()
    }
}