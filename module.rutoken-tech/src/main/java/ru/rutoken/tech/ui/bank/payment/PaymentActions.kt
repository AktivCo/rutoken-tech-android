/*
 * Copyright (c) 2024, Aktiv-Soft JSC.
 * See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.tech.ui.bank.payment

import android.content.Context
import androidx.annotation.WorkerThread
import org.bouncycastle.cert.X509CertificateHolder
import ru.rutoken.pkcs11wrapper.rutoken.main.RtPkcs11Session
import ru.rutoken.tech.ca.LocalCA
import ru.rutoken.tech.pkcs11.findobjects.findGost256CertificateByCkaId
import ru.rutoken.tech.pkcs11.findobjects.findGost256KeyPairByCkaId
import ru.rutoken.tech.ui.bank.payments.Payment
import ru.rutoken.tech.usecase.CmsOperationProvider
import ru.rutoken.tech.usecase.CmsOperations
import ru.rutoken.tech.utils.decoded
import ru.rutoken.tech.utils.toBase64String
import java.time.LocalDateTime

@WorkerThread
fun RtPkcs11Session.signPayment(
    payment: Payment,
    certificateCkaId: ByteArray,
    applicationContext: Context,
    provider: CmsOperationProvider = CmsOperationProvider.PKCS11_WRAPPER
) {
    payment.apply {
        actionResultData = CmsOperations.signDetached(
            provider,
            this@signPayment,
            getActionData(applicationContext).decoded,
            findGost256KeyPairByCkaId(certificateCkaId).privateKey,
            findGost256CertificateByCkaId(certificateCkaId),
            listOf(X509CertificateHolder(LocalCA.rootCertificate))
        ).toBase64String()

        actionTime = LocalDateTime.now()
    }
}