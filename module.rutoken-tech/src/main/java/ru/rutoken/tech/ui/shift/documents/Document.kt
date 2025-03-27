/*
 * Copyright (c) 2025, Aktiv-Soft JSC.
 * See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.tech.ui.shift.documents

import java.time.LocalDate
import java.util.Base64

data class Document(
    val title: String,
    val date: LocalDate,
    val organization: String,
    val signedCms: ByteArray? = null,
    val displayPageIndex: Int = 0,
) {
    val fileName: String = "$title.pdf"
    val signatureFileName: String = "$title.sig"
    val assetName: String = "shiftdocuments/$fileName"

    val base64SignatureBytes: ByteArray? get() = signedCms?.let { Base64.getEncoder().encode(it) }
}

data class SignedDocumentsGroup(
    val documents: List<Document>,
    val date: LocalDate,
    val signatories: List<String>,
)