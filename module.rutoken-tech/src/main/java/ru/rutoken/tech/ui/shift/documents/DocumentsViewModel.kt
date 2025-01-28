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
import ru.rutoken.tech.session.AppSessionHolder
import ru.rutoken.tech.session.ShiftUserLoginAppSession
import ru.rutoken.tech.session.requireShiftUserLoginSession

class DocumentsViewModel(private val sessionHolder: AppSessionHolder) : ViewModel() {
    // ShiftUserLoginAppSession instance MUST exist by the time this ViewModel is instantiated
    private val shiftUserLoginSession: ShiftUserLoginAppSession
        get() = sessionHolder.requireShiftUserLoginSession()

    private val _documents = MutableLiveData(shiftUserLoginSession.documents)
    val documents: LiveData<List<Document>> get() = _documents

    private val _signedDocuments = MutableLiveData(shiftUserLoginSession.signedDocuments)
    val signedDocuments: LiveData<List<SignedDocumentsGroup>> get() = _signedDocuments

    fun onShareClicked(documents: SignedDocumentsGroup) {
        //TODO
    }

    @MainThread
    fun onSignatoriesClicked(documents: SignedDocumentsGroup) {
        //TODO
    }

    @MainThread
    fun onResetDocumentsClicked() {
        //TODO
    }
}