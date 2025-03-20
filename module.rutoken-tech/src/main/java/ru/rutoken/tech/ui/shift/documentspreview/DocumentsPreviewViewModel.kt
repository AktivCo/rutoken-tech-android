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
import ru.rutoken.tech.session.AppSessionHolder
import ru.rutoken.tech.session.DocumentsPreviewInfo
import ru.rutoken.tech.session.ShiftUserLoginAppSession
import ru.rutoken.tech.session.requireShiftUserLoginSession

class DocumentsPreviewViewModel(private val sessionHolder: AppSessionHolder) : ViewModel() {
    // ShiftUserLoginAppSession instance MUST exist by the time this ViewModel is instantiated
    private val shiftUserLoginSession: ShiftUserLoginAppSession
        get() = sessionHolder.requireShiftUserLoginSession()

    private val _documents = MutableLiveData(shiftUserLoginSession.chosenDocuments)
    val documents: LiveData<DocumentsPreviewInfo> get() = _documents

    private val isDocumentsSignedBefore = isDocumentsSigned()

    private val _showSignButton = MutableLiveData(!isDocumentsSignedBefore)
    val showSignButton: LiveData<Boolean> get() = _showSignButton

    private val _showFinishSigningDialog = MutableLiveData(false)
    val showFinishSigningDialog: LiveData<Boolean> get() = _showFinishSigningDialog

    private val _navigateBack = MutableLiveData(false)
    val navigateBack: LiveData<Boolean> get() = _navigateBack

    fun onDocumentShareClick() {
        // TODO: Not yet implemented
    }

    @MainThread
    fun hideFinishSigningDialog() {
        _showFinishSigningDialog.value = false
        _navigateBack.value = true
    }

    @MainThread
    fun updateSignState() {
        val isDocumentsSignedNow = isDocumentsSigned()

        _showSignButton.value = !isDocumentsSignedBefore && !isDocumentsSignedNow
        _showFinishSigningDialog.value = !isDocumentsSignedBefore && isDocumentsSignedNow
    }

    private fun isDocumentsSigned() = shiftUserLoginSession.chosenDocuments.documents.any { it.signedCms != null }
}
