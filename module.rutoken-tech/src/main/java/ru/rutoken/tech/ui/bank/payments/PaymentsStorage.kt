/*
 * Copyright (c) 2024, Aktiv-Soft JSC.
 * See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.tech.ui.bank.payments

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.bouncycastle.cert.X509CertificateHolder
import org.bouncycastle.cms.CMSAlgorithm.GOST28147_GCFB
import ru.rutoken.tech.bank.BANK_CERTIFICATE_GOST
import ru.rutoken.tech.bank.BANK_PRIVATE_KEY_GOST
import ru.rutoken.tech.bouncycastle.BouncyCastleCmsOperations
import ru.rutoken.tech.ca.LocalCA
import ru.rutoken.tech.utils.base64ToX509CertificateHolder
import ru.rutoken.tech.utils.toBase64String
import java.time.LocalDate

private val initialPaymentsStorage
    get() = listOf(
        Payment(
            title = "Платежное поручение №10",
            date = LocalDate.of(2023, 8, 8),
            amount = "80 800 ₽",
            organization = "АО «Глобус Вайт»",
            userActionType = UserActionType.SIGN
        ),
        Payment(
            title = "Платежное поручение №121",
            date = LocalDate.of(2023, 8, 15),
            amount = "100 000 ₽",
            organization = "АО «Печатник»",
            userActionType = UserActionType.SIGN
        ),
        Payment(
            title = "Платежное поручение №122",
            date = LocalDate.of(2023, 7, 13),
            amount = "60 000 ₽",
            organization = "АО «Компьютерные технологии»",
            userActionType = UserActionType.SIGN
        ),
        Payment(
            title = "Платежное поручение №123",
            date = LocalDate.of(2023, 5, 18),
            amount = "200 500 ₽",
            organization = "АО «Компьютерные технологии»",
            userActionType = UserActionType.SIGN
        ),
        Payment(
            title = "Платежное поручение №124",
            date = LocalDate.of(2023, 1, 23),
            amount = "200 500 ₽",
            organization = "АО «ИнфоНет»",
            userActionType = UserActionType.SIGN
        ),

        Payment(
            title = "Платежное поручение №543",
            date = LocalDate.of(2023, 2, 27),
            amount = "800 300 ₽",
            organization = "ООО «КО-НЕО»",
            userActionType = UserActionType.VERIFY
        ),
        Payment(
            title = "Платежное поручение №654",
            date = LocalDate.of(2022, 7, 24),
            amount = "300 000 ₽",
            organization = "ООО «Мобайл»",
            userActionType = UserActionType.VERIFY
        ),
        Payment(
            title = "Платежное поручение №673",
            date = LocalDate.of(2023, 1, 1),
            amount = "3 000 ₽",
            organization = "ООО «Фрешлайтс»",
            userActionType = UserActionType.VERIFY
        ),
        Payment(
            title = "Платежное поручение №981",
            date = LocalDate.of(2023, 2, 18),
            amount = "45 000 ₽",
            organization = "ООО «Тренд Хоум»",
            userActionType = UserActionType.VERIFY
        ),
        Payment(
            title = "Платежное поручение №4134",
            date = LocalDate.of(2023, 1, 1),
            amount = "3 000 ₽",
            organization = "ПАО «Инвест Банк»",
            userActionType = UserActionType.VERIFY
        ),

        Payment(
            title = "Инкассовое поручение №15",
            date = LocalDate.of(2023, 8, 30),
            amount = "700 350 ₽",
            organization = "ПАО «Лого ЭКСПО»",
            userActionType = UserActionType.ENCRYPT
        ),
        Payment(
            title = "Инкассовое поручение №35",
            date = LocalDate.of(2023, 7, 13),
            amount = "200 500 ₽",
            organization = "АО «Компьютерные технологии»",
            userActionType = UserActionType.ENCRYPT
        ),
        Payment(
            title = "Инкассовое поручение №76",
            date = LocalDate.of(2023, 4, 27),
            amount = "2 000 000 ₽",
            organization = "АО «Азбука Классик»",
            userActionType = UserActionType.ENCRYPT
        ),
        Payment(
            title = "Инкассовое поручение №98",
            date = LocalDate.of(2023, 6, 30),
            amount = "301 000 ₽",
            organization = "АО «АктивКо»",
            userActionType = UserActionType.ENCRYPT
        ),
        Payment(
            title = "Инкассовое поручение №127",
            date = LocalDate.of(2024, 5, 13),
            amount = "200 000 ₽",
            organization = "АО «Лидер технологий»",
            userActionType = UserActionType.ENCRYPT
        ),

        Payment(
            title = "Инкассовое поручение №333",
            date = LocalDate.of(2023, 10, 26),
            amount = "487 000 ₽",
            organization = "АО «Шаоми»",
            userActionType = UserActionType.DECRYPT
        ),
        Payment(
            title = "Инкассовое поручение №345",
            date = LocalDate.of(2023, 8, 28),
            amount = "45 000 ₽",
            organization = "ООО «Тренд Хоум»",
            userActionType = UserActionType.DECRYPT
        ),
        Payment(
            title = "Инкассовое поручение №567",
            date = LocalDate.of(2023, 10, 26),
            amount = "560 000 ₽",
            organization = "ПАО «Квартирный вопрос»",
            userActionType = UserActionType.DECRYPT
        ),
        Payment(
            title = "Инкассовое поручение №789",
            date = LocalDate.of(2023, 8, 15),
            amount = "40 000 ₽",
            organization = "ООО «Крипто-Экс»",
            userActionType = UserActionType.DECRYPT
        ),
        Payment(
            title = "Инкассовое поручение №981",
            date = LocalDate.of(2023, 2, 18),
            amount = "45 000 ₽",
            organization = "ООО «Тренд Хоум»",
            userActionType = UserActionType.DECRYPT
        )
    )

suspend fun getInitialPaymentsStorage(context: Context, userCertificateHolder: X509CertificateHolder): List<Payment> {
    val paymentsStorage = initialPaymentsStorage

    withContext(Dispatchers.IO) {
        paymentsStorage.filter { it.userActionType == UserActionType.DECRYPT }.forEach { payment ->
            payment.initialActionData =
                BouncyCastleCmsOperations.encrypt(payment.readFile(context), userCertificateHolder, GOST28147_GCFB)
                    .toBase64String()
        }

        paymentsStorage.filter { it.userActionType == UserActionType.VERIFY }.forEachIndexed { index, payment ->
            val dataToSign: ByteArray
            val additionalCertificates: List<X509CertificateHolder>

            when (index) {
                // Make a valid signature and chain
                0, 1 -> {
                    dataToSign = payment.readFile(context)
                    // TODO: add new root certificate to make chain longer
                    additionalCertificates = listOf(X509CertificateHolder(LocalCA.rootCertificate))
                }
                // Make a valid signature, but break chain
                2 -> {
                    dataToSign = payment.readFile(context)
                    // TODO: add new root certificate and remove LocalCA.rootCertificate from chain
                    additionalCertificates = listOf(X509CertificateHolder(LocalCA.rootCertificate))
                }
                // Make an invalid signature by corrupting data before signing
                else -> {
                    dataToSign = payment.readFile(context) + 0b1
                    // Chain verification makes no sense when the Signature itself is invalid
                    additionalCertificates = listOf()
                }
            }

            payment.initialActionData = BouncyCastleCmsOperations.signDetachedGost256(
                dataToSign,
                BANK_PRIVATE_KEY_GOST,
                base64ToX509CertificateHolder(BANK_CERTIFICATE_GOST),
                additionalCertificates
            ).toBase64String()
        }
    }

    return paymentsStorage
}