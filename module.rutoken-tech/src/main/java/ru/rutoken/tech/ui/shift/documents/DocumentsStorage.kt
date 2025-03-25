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
            date = LocalDate.of(2023, 2, 9),
            organization = "ООО “МосЭнерго”",
            displayPageIndex = 1
        ),
        Document(
            title = "Инструктаж по ТБ №3",
            date = LocalDate.of(2023, 5, 30),
            organization = "ООО “МосЭнерго”",
            displayPageIndex = 0
        ),
        Document(
            title = "Инструктаж по ТБ №2",
            date = LocalDate.of(2024, 8, 16),
            organization = "ООО “МосЭнерго”",
            displayPageIndex = 0
        ),
        Document(
            title = "Инструктаж по ТБ №1",
            date = LocalDate.of(2024, 1, 10),
            organization = "ООО “МосЭнерго”",
            displayPageIndex = 0
        ),
        Document(
            title = "Наряд-допуск №4",
            date = LocalDate.of(2024, 9, 10),
            organization = "ООО “Цмик”",
            displayPageIndex = 0
        ),
        Document(
            title = "Наряд-допуск №3",
            date = LocalDate.of(2024, 11, 19),
            organization = "АО “Кузнец”",
            displayPageIndex = 0
        ),
        Document(
            title = "Наряд-допуск №2",
            date = LocalDate.of(2024, 12, 10),
            organization = "ООО “БИК-Комфорт”",
            displayPageIndex = 0
        ),
        Document(
            title = "Наряд-допуск №1",
            date = LocalDate.of(2024, 12, 15),
            organization = "ООО “Сигма”",
            displayPageIndex = 0
        ),
        Document(
            title = "Журнал работ №2",
            date = LocalDate.of(2024, 12, 10),
            organization = "ООО “БИК-Комфорт”",
            displayPageIndex = 1
        ),
        Document(
            title = "Журнал работ №1",
            date = LocalDate.of(2024, 12, 23),
            organization = "АО “СтройМаш”",
            displayPageIndex = 1
        )
    )