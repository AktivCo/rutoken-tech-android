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
import ru.rutoken.tech.session.ShiftUserLoginAppSession
import ru.rutoken.tech.session.requireShiftUserLoginSession
import ru.rutoken.tech.ui.shift.documents.Document

class DocumentsPreviewViewModel(private val sessionHolder: AppSessionHolder) : ViewModel() {
    // ShiftUserLoginAppSession instance MUST exist by the time this ViewModel is instantiated
    private val shiftUserLoginSession: ShiftUserLoginAppSession
        get() = sessionHolder.requireShiftUserLoginSession()

    private val _documentsToSign = MutableLiveData(shiftUserLoginSession.documentsToSign)
    val documentsToSign: LiveData<List<Document>> get() = _documentsToSign

    @MainThread
    fun onSignClicked() {
        // TODO: Not yet implemented
    }
}
