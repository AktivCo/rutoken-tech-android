/*
 * Copyright (c) 2025, Aktiv-Soft JSC.
 * See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.tech.ui.shift.utils

import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.rutoken.tech.helpers.FilesHelper
import ru.rutoken.tech.ui.shift.documents.Document
import java.io.File

suspend fun shareSignedDocumentsZip(
    filesHelper: FilesHelper,
    documents: List<Document>,
    zipName: String,
    onSharedFilesReady: (List<File>) -> Unit,
) = withContext(Dispatchers.IO) {
    val signedDocuments = makeSignedDocumentsList(filesHelper, documents)
    onSharedFilesReady(listOf(filesHelper.createZipFileInCache(signedDocuments, zipName)))
}

suspend fun saveSignedDocumentsZip(
    filesHelper: FilesHelper,
    documents: List<Document>,
    zipName: String,
    uri: Uri,
) = withContext(Dispatchers.IO) {
    val signedDocuments = makeSignedDocumentsList(filesHelper, documents)
    filesHelper.writeFileByUri(filesHelper.createZipFileInCache(signedDocuments, zipName), uri)
}

private suspend fun makeSignedDocumentsList(filesHelper: FilesHelper, documents: List<Document>): List<File> =
    buildList {
        documents.forEach {
            add(filesHelper.copyAssetToCache(it.assetName, it.fileName))
            add(filesHelper.createSignatureFile(it.signatureFileName, it.base64SignatureBytes!!))
        }
    }
