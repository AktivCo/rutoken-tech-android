/*
 * Copyright (c) 2025, Aktiv-Soft JSC.
 * See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.tech.ui.shift.documents

import androidx.annotation.MainThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.rutoken.tech.helpers.FilesHelper
import ru.rutoken.tech.repository.shift.signeddocument.ShiftSignedDocumentRepository
import ru.rutoken.tech.session.AppSessionHolder
import ru.rutoken.tech.session.DocumentsPreviewInfo
import ru.rutoken.tech.session.ShiftUserLoginAppSession
import ru.rutoken.tech.session.requireShiftUserLoginSession
import ru.rutoken.tech.ui.shift.utils.shareSignedDocumentsZip
import java.io.File
import java.time.LocalDate

class DocumentsViewModel(
    private val sessionHolder: AppSessionHolder,
    private val shiftSignedDocumentRepository: ShiftSignedDocumentRepository,
    private val filesHelper: FilesHelper,
    private val onNavigateToDocumentsPreview: () -> Unit,
) : ViewModel() {
    // ShiftUserLoginAppSession instance MUST exist by the time this ViewModel is instantiated
    private val shiftUserLoginSession: ShiftUserLoginAppSession
        get() = sessionHolder.requireShiftUserLoginSession()

    private val _documents = MutableLiveData<Map<LocalDate, List<Document>>>()
    val documents: LiveData<Map<LocalDate, List<Document>>> get() = _documents

    private val _signedDocuments = MutableLiveData<Map<LocalDate, List<SignedDocumentsGroup>>>()
    val signedDocuments: LiveData<Map<LocalDate, List<SignedDocumentsGroup>>>
        get() = _signedDocuments

    private val _documentsToSign = MutableLiveData(emptyList<Document>())
    val documentsToSign: LiveData<List<Document>> get() = _documentsToSign

    private val _documentsGroupSignatories = MutableLiveData(emptyList<String>())
    val documentsGroupSignatories: LiveData<List<String>> get() = _documentsGroupSignatories

    @MainThread
    fun onShareClicked(documents: SignedDocumentsGroup, onSharedFilesReady: (List<File>) -> Unit) {
        viewModelScope.launch {
            shareSignedDocumentsZip(filesHelper, documents.documents, onSharedFilesReady)
        }
    }

    @MainThread
    fun onSignatoriesBottomSheetClose() {
        _documentsGroupSignatories.value = emptyList()
    }

    @MainThread
    fun onSignatoriesClicked(documents: SignedDocumentsGroup) {
        _documentsGroupSignatories.value = documents.signatories
    }

    @MainThread
    fun onDocumentSelected(document: Document) {
        val currentDocumentsToSign = _documentsToSign.value!!
        _documentsToSign.value = currentDocumentsToSign.toMutableList().apply {
            if (!currentDocumentsToSign.contains(document)) add(document)
            else remove(document)
        }
    }

    @MainThread
    fun onSignedDocumentClicked(document: Document, documentsGroup: SignedDocumentsGroup) {
        with(documentsGroup.documents) {
            shiftUserLoginSession.chosenDocuments = DocumentsPreviewInfo(this, indexOf(document))
        }
        onNavigateToDocumentsPreview()
    }

    @MainThread
    fun onNavigateToPreview() {
        shiftUserLoginSession.chosenDocuments = DocumentsPreviewInfo(_documentsToSign.value!!)
        _documentsToSign.value = emptyList()
        onNavigateToDocumentsPreview()
    }

    @MainThread
    fun onResetSelectedDocumentsClicked() {
        _documentsToSign.value = emptyList()
    }

    @MainThread
    fun onResetDocumentsClicked() {
        viewModelScope.launch(Dispatchers.IO) {
            shiftSignedDocumentRepository.deleteAllSignedDocumentsBySessionId(shiftUserLoginSession.userId)

            shiftUserLoginSession.documents = initialDocumentsStorage
            shiftUserLoginSession.signedDocuments = emptyList()

            updateDocumentsFlow()
        }
    }

    suspend fun updateDocumentsFlow() = withContext(Dispatchers.Default) {
        launch {
            shiftUserLoginSession.documents.sortedByDescending { it.date }
                .groupBy { it.date }
                .let { _documents.postValue(it) }
        }

        launch {
            shiftUserLoginSession.signedDocuments.sortedByDescending { it.date }
                .groupBy { it.date }
                .let { _signedDocuments.postValue(it) }
        }
    }
}
