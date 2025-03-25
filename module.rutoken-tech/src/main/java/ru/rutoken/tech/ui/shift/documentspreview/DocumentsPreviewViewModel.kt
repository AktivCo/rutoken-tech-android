/*
 * Copyright (c) 2025, Aktiv-Soft JSC.
 * See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.tech.ui.shift.documentspreview

import android.content.Context
import android.net.Uri
import androidx.annotation.MainThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ru.rutoken.tech.R
import ru.rutoken.tech.helpers.FilesHelper
import ru.rutoken.tech.session.AppSessionHolder
import ru.rutoken.tech.session.DocumentsPreviewInfo
import ru.rutoken.tech.session.ShiftUserLoginAppSession
import ru.rutoken.tech.session.requireShiftUserLoginSession
import ru.rutoken.tech.ui.vmdelegate.SaveFileDelegate
import ru.rutoken.tech.ui.vmdelegate.ShareFilesDelegate
import ru.rutoken.tech.utils.toDateString
import java.io.File
import java.time.Instant
import java.util.Date

class DocumentsPreviewViewModel(
    private val appContext: Context,
    private val sessionHolder: AppSessionHolder,
    filesHelper: FilesHelper,
) : ViewModel() {
    // ShiftUserLoginAppSession instance MUST exist by the time this ViewModel is instantiated
    private val shiftUserLoginSession: ShiftUserLoginAppSession
        get() = sessionHolder.requireShiftUserLoginSession()

    private val saveFileDelegate = SaveFileDelegate(viewModelScope, filesHelper)
    private val shareFilesDelegate = ShareFilesDelegate(viewModelScope, filesHelper)

    private val _documents = MutableLiveData(shiftUserLoginSession.chosenDocuments)
    val documents: LiveData<DocumentsPreviewInfo> get() = _documents

    private val areDocumentsSignedBefore = checkDocumentsSignaturePresent()

    private val _areDocumentsSigned = MutableLiveData(areDocumentsSignedBefore)
    val areDocumentsSigned: LiveData<Boolean> get() = _areDocumentsSigned

    private val _showFinishSigningDialog = MutableLiveData(false)
    val showFinishSigningDialog: LiveData<Boolean> get() = _showFinishSigningDialog

    private val _navigateBack = MutableLiveData(false)
    val navigateBack: LiveData<Boolean> get() = _navigateBack

    @MainThread
    fun onShareClicked(onSharedFilesReady: (List<File>) -> Unit) =
        shareFilesDelegate.shareSignedDocumentsZip(
            shiftUserLoginSession.chosenDocuments.documents,
            getSharedZipName(),
            onSharedFilesReady
        )

    fun saveDocumentsByUriAsZip(uri: Uri?) =
        saveFileDelegate.saveDocumentsByUriAsZip(
            uri,
            getSharedZipName(),
            shiftUserLoginSession.chosenDocuments.documents
        )

    @MainThread
    fun hideFinishSigningDialog() {
        _showFinishSigningDialog.value = false
        _navigateBack.value = true
    }

    @MainThread
    fun updateSignState() {
        val areDocumentsSignedNow = checkDocumentsSignaturePresent()

        _areDocumentsSigned.value = areDocumentsSignedBefore || areDocumentsSignedNow
        _showFinishSigningDialog.value = !areDocumentsSignedBefore && areDocumentsSignedNow
    }

    fun getSharedZipName(): String =
        appContext.getString(R.string.shared_documents_zip_file_name, Date.from(Instant.now()).toDateString())

    private fun checkDocumentsSignaturePresent() =
        shiftUserLoginSession.chosenDocuments.documents.any { it.signedCms != null }
}
