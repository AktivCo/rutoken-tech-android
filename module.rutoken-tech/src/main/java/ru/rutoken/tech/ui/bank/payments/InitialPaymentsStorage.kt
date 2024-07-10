/*
 * Copyright (c) 2024, Aktiv-Soft JSC.
 * See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.tech.ui.bank.payments

import java.time.LocalDate
import java.time.LocalDateTime

// TODO: fill with real data
val initialPaymentsStorage = listOf(
    Payment(
        title = "Платежное поручение №121",
        date = LocalDate.of(2023, 7, 12),
        amount = "14 500 ₽",
        organization = "ОАО Нефтегаз",
        operationType = OperationType.SIGN
    ),
    Payment(
        title = "Платежное поручение №122",
        date = LocalDate.of(2023, 8, 12),
        amount = "14 500 ₽",
        organization = "ОАО Нефтегаз",
        operationType = OperationType.VERIFY
    ),
    Payment(
        title = "Инкассовое поручение №121",
        date = LocalDate.of(2023, 7, 12),
        amount = "4 500 ₽",
        organization = "ОАО Нефтегаз",
        operationType = OperationType.ENCRYPT
    ),
    Payment(
        title = "Инкассовое поручение №122",
        date = LocalDate.of(2023, 7, 12),
        amount = "14 500 ₽",
        organization = "ОАО Нефтегаз",
        operationType = OperationType.DECRYPT
    ),

    Payment(
        title = "Платежное поручение №121",
        date = LocalDate.of(2023, 7, 12),
        amount = "14 500 ₽",
        organization = "ОАО Нефтегаз",
        operationType = OperationType.SIGN,
        operationTime = LocalDateTime.now()
    ),
    Payment(
        title = "Платежное поручение №122",
        date = LocalDate.of(2023, 8, 12),
        amount = "14 500 ₽",
        organization = "ОАО Нефтегаз",
        operationType = OperationType.VERIFY,
        operationTime = LocalDateTime.now()
    ),
    Payment(
        title = "Инкассовое поручение №121",
        date = LocalDate.of(2023, 7, 12),
        amount = "4 500 ₽",
        organization = "ОАО Нефтегаз",
        operationType = OperationType.ENCRYPT,
        operationTime = LocalDateTime.now()
    ),
    Payment(
        title = "Инкассовое поручение №122",
        date = LocalDate.of(2023, 7, 12),
        amount = "14 500 ₽",
        organization = "ОАО Нефтегаз",
        operationType = OperationType.DECRYPT,
        operationTime = LocalDateTime.now()
    )
)