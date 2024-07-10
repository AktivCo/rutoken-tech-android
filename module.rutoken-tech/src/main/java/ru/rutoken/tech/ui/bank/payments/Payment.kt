/*
 * Copyright (c) 2024, Aktiv-Soft JSC.
 * See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.tech.ui.bank.payments

import androidx.compose.runtime.Composable
import ru.rutoken.tech.ui.components.AppIcons
import java.time.LocalDate
import java.time.LocalDateTime

enum class OperationType {
    SIGN,
    VERIFY,
    ENCRYPT,
    DECRYPT
}

data class Payment(
    val title: String,
    val date: LocalDate,
    val amount: String,
    val organization: String,
    val operationType: OperationType,
    var operationTime: LocalDateTime? = null,
)

@Composable
fun Payment.Icon(isArchived: Boolean) = when (operationType) {
    OperationType.SIGN -> AppIcons.PaymentToSign(isArchived)
    OperationType.VERIFY -> AppIcons.PaymentToVerify(isArchived)
    OperationType.ENCRYPT -> AppIcons.PaymentToEncrypt(isArchived)
    OperationType.DECRYPT -> AppIcons.PaymentToDecrypt(isArchived)
}

fun Payment.isIncoming() = operationType == OperationType.VERIFY || operationType == OperationType.DECRYPT

fun Payment.isArchived() = operationTime != null