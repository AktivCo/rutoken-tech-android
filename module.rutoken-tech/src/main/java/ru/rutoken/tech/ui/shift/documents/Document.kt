/*
 * Copyright (c) 2025, Aktiv-Soft JSC.
 * See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.tech.ui.shift.documents

import android.content.Context
import java.time.LocalDate

data class Document(
    val title: String,
    val date: LocalDate,
    val organization: String,
    val signedCms: ByteArray? = null,
    val displayPageIndex: Int = 0,
) {
    val assetName: String = "shiftdocuments/$title.pdf"

    fun readFile(context: Context) = context.assets.open(assetName).use { it.readBytes() }
}

data class SignedDocumentsGroup(
    val documents: List<Document>,
    val date: LocalDate,
    val signatories: List<String>
)