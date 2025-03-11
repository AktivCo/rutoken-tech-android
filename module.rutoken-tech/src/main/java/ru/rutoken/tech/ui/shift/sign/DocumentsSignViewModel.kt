/*
 * Copyright (c) 2025, Aktiv-Soft JSC.
 * See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.tech.ui.shift.sign

import androidx.annotation.MainThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class DocumentsSignViewModel : ViewModel() {
    // TODO: Remove hardcoded list before merge
    private val _signatories = MutableLiveData(
        listOf(
            "Петров Петр Петрович",
            "Марьева Мария Михайловна",
            "Иванов Иван Иванович",
            "Аннова Анна Алексеевна",
            "Михайлов Михаил Михайлович"
        )
    )
    val signatories: LiveData<List<String>> get() = _signatories

    private val _showFinishSigningDialog = MutableLiveData(false)
    val showFinishSigningDialog: LiveData<Boolean> get() = _showFinishSigningDialog

    fun onSignClick() {
        // TODO: Not yet implemented
    }

    @MainThread
    fun onFinishSigningClick() {
        _showFinishSigningDialog.value = true
    }

    fun onDocumentShareClick() {
        // TODO: Not yet implemented
    }
}
