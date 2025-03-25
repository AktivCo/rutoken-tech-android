/*
 * Copyright (c) 2025, Aktiv-Soft JSC.
 * See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.tech.ui.vmdelegate

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import ru.rutoken.tech.helpers.FilesHelper
import ru.rutoken.tech.ui.shift.documents.Document
import ru.rutoken.tech.ui.shift.utils.shareSignedDocumentsZip
import java.io.File

class ShareFilesDelegate(
    override val delegateScope: CoroutineScope,
    private val filesHelper: FilesHelper,
) : ViewModelDelegate {
    fun shareSignedDocumentsZip(
        sharedDocuments: List<Document>,
        zipName: String,
        onSharedFilesReady: (List<File>) -> Unit,
    ) {
        delegateScope.launch {
            shareSignedDocumentsZip(filesHelper, sharedDocuments, zipName, onSharedFilesReady)
        }
    }
}
