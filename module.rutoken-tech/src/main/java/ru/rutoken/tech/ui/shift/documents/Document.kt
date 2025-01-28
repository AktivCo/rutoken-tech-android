/*
 * Copyright (c) 2025, Aktiv-Soft JSC.
 * See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.tech.ui.shift.documents

import java.time.LocalDate

data class Document(
    val title: String,
    val date: LocalDate,
    val organization: String,
)

data class SignedDocumentsGroup(
    val documents: List<Document>,
    val date: LocalDate
)