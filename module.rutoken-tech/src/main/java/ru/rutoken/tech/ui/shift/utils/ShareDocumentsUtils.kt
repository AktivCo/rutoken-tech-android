/*
 * Copyright (c) 2025, Aktiv-Soft JSC.
 * See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.tech.ui.shift.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.rutoken.tech.R
import ru.rutoken.tech.helpers.FilesHelper
import ru.rutoken.tech.ui.shift.documents.Document
import ru.rutoken.tech.utils.toDateString
import java.io.File
import java.time.Instant
import java.util.Date

suspend fun shareSignedDocumentsZip(
    filesHelper: FilesHelper,
    documents: List<Document>,
    onSharedFilesReady: (List<File>) -> Unit,
) = withContext(Dispatchers.IO) {
    val documentsToShare = documents.map { filesHelper.copyAssetToCache(it.assetName, it.fileName) }
    val signatureFilesToShare =
        documents.map { filesHelper.createSignatureFile(it.signatureFileName, it.base64SignatureBytes!!) }
    val currentDate = Date.from(Instant.now()).toDateString()
    onSharedFilesReady(
        listOf(
            filesHelper.createZipFile(
                documentsToShare + signatureFilesToShare,
                R.string.shared_documents_zip_file_name,
                currentDate
            )
        )
    )
}
