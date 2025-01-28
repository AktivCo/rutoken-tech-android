/*
 * Copyright (c) 2025, Aktiv-Soft JSC.
 * See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.tech.ui.shift.documents

import java.time.LocalDate

val initialDocumentsStorage
    get() = listOf(
        Document(
            title = "Инструктаж по ТБ №4",
            date = LocalDate.now(),
            organization = "ООО “МосЭнерго”",
        ),
        Document(
            title = "Инструктаж по ТБ №3",
            date = LocalDate.of(2023, 12, 10),
            organization = "ООО “МосЭнерго",
        ),
        Document(
            title = "Инструктаж по ТБ №2",
            date = LocalDate.of(2023, 12, 10),
            organization = "ООО “МосЭнерго",
        ),
        Document(
            title = "Инструктаж по ТБ №1",
            date = LocalDate.of(2023, 12, 10),
            organization = "ООО “МосЭнерго",
        )
    )

val initialSignedDocuments = listOf( // TODO delete after implemented signing the document
    SignedDocumentsGroup(
        documents = listOf(
            initialDocumentsStorage[1],
            initialDocumentsStorage[1],
            initialDocumentsStorage[2]
        ),
        date = LocalDate.now()
    ),
    SignedDocumentsGroup(
        documents = listOf(
            initialDocumentsStorage[1],
            initialDocumentsStorage[1],
            initialDocumentsStorage[2]
        ),
        date = LocalDate.now()
    ),
    SignedDocumentsGroup(
        documents = listOf(initialDocumentsStorage[1]),
        date = LocalDate.of(2023, 12, 10)
    )
)