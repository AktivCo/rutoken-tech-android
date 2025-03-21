/*
 * Copyright (c) 2025, Aktiv-Soft JSC.
 * See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.tech.ui.shift.documentspreview

import androidx.annotation.MainThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.rutoken.tech.helpers.FilesHelper
import ru.rutoken.tech.session.AppSessionHolder
import ru.rutoken.tech.session.DocumentsPreviewInfo
import ru.rutoken.tech.session.ShiftUserLoginAppSession
import ru.rutoken.tech.session.requireShiftUserLoginSession
import ru.rutoken.tech.ui.shift.utils.shareSignedDocumentsZip
import java.io.File

class DocumentsPreviewViewModel(
    private val sessionHolder: AppSessionHolder,
    private val filesHelper: FilesHelper,
) : ViewModel() {
    // ShiftUserLoginAppSession instance MUST exist by the time this ViewModel is instantiated
    private val shiftUserLoginSession: ShiftUserLoginAppSession
        get() = sessionHolder.requireShiftUserLoginSession()

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
    fun onShareClicked(onSharedFilesReady: (List<File>) -> Unit) {
        viewModelScope.launch {
            shareSignedDocumentsZip(filesHelper, shiftUserLoginSession.chosenDocuments.documents, onSharedFilesReady)
        }
    }

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

    private fun checkDocumentsSignaturePresent() =
        shiftUserLoginSession.chosenDocuments.documents.any { it.signedCms != null }
}
