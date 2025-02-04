/*
 * Copyright (c) 2025, Aktiv-Soft JSC.
 * See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.tech.repository.shift.signeddocument

import ru.rutoken.tech.ui.shift.documents.SignedDocumentsGroup

interface ShiftSignedDocumentRepository {
    suspend fun addSignedDocuments(documentsGroup: SignedDocumentsGroup, sessionId: Int)

    suspend fun getAllSignedDocumentsBySessionId(sessionId: Int): List<SignedDocumentsGroup>
}