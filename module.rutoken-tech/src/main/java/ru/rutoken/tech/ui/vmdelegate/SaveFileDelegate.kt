/*
 * Copyright (c) 2025, Aktiv-Soft JSC.
 * See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.tech.ui.vmdelegate

import android.net.Uri
import androidx.annotation.MainThread
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import ru.rutoken.tech.helpers.FilesHelper
import ru.rutoken.tech.ui.shift.documents.Document
import ru.rutoken.tech.ui.shift.utils.saveSignedDocumentsZip
import ru.rutoken.tech.utils.logw

class SaveFileDelegate(
    override val delegateScope: CoroutineScope,
    private val filesHelper: FilesHelper,
) : ViewModelDelegate {
    @MainThread
    fun saveDocumentsByUriAsZip(uri: Uri?, zipName: String, documents: List<Document>) {
        if (uri == null) {
            logw { "The file could not be saved to the internal storage. The passed URI is null." }
            return
        }

        delegateScope.launch {
            saveSignedDocumentsZip(filesHelper, documents, zipName, uri)
        }
    }
}
