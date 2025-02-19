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
import ru.rutoken.tech.repository.shift.signeddocument.ShiftSignedDocumentRepository
import ru.rutoken.tech.session.AppSessionHolder
import ru.rutoken.tech.session.ShiftUserLoginAppSession
import ru.rutoken.tech.session.requireShiftUserLoginSession

class DocumentsViewModel(
    private val sessionHolder: AppSessionHolder,
    private val shiftSignedDocumentRepository: ShiftSignedDocumentRepository
) : ViewModel() {
    // ShiftUserLoginAppSession instance MUST exist by the time this ViewModel is instantiated
    private val shiftUserLoginSession: ShiftUserLoginAppSession
        get() = sessionHolder.requireShiftUserLoginSession()

    private val _documents = MutableLiveData(shiftUserLoginSession.documents)
    val documents: LiveData<List<Document>> get() = _documents

    private val _signedDocuments = MutableLiveData(shiftUserLoginSession.signedDocuments)
    val signedDocuments: LiveData<List<SignedDocumentsGroup>> get() = _signedDocuments

    private val _documentsGroupSignatories = MutableLiveData(emptyList<String>())
    val documentsGroupSignatories: LiveData<List<String>> get() = _documentsGroupSignatories

    fun onShareClicked(documents: SignedDocumentsGroup) {
        //TODO
    }

    fun onSignatoriesBottomSheetClose() {
        _documentsGroupSignatories.value = emptyList()
    }

    @MainThread
    fun onSignatoriesClicked(documents: SignedDocumentsGroup) {
        _documentsGroupSignatories.value = documents.signatories
    }

    @MainThread
    fun onResetDocumentsClicked() {
        viewModelScope.launch(Dispatchers.IO) {
            shiftSignedDocumentRepository.deleteAllSignedDocumentsBySessionId(shiftUserLoginSession.userId)

            shiftUserLoginSession.documents = initialDocumentsStorage
            _documents.postValue(shiftUserLoginSession.documents)

            shiftUserLoginSession.signedDocuments = emptyList()
            _signedDocuments.postValue(shiftUserLoginSession.signedDocuments)
        }
    }
}