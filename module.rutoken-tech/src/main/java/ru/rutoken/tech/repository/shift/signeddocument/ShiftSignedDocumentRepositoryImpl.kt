/*
 * Copyright (c) 2025, Aktiv-Soft JSC.
 * See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.tech.repository.shift.signeddocument

import ru.rutoken.tech.database.Database
import ru.rutoken.tech.database.shift.document.SignedDocumentEntity
import ru.rutoken.tech.ui.shift.documents.Document
import ru.rutoken.tech.ui.shift.documents.SignedDocumentsGroup
import java.util.UUID

class ShiftSignedDocumentRepositoryImpl(database: Database) : ShiftSignedDocumentRepository {
    private val signedDocumentDao = database.signedDocumentDao()

    override suspend fun addSignedDocuments(
        documentsGroup: SignedDocumentsGroup,
        sessionId: Int,
    ) {
        val signGroupId = UUID.randomUUID().toString()

        documentsGroup.documents.forEach { document ->
            val entity = SignedDocumentEntity(
                fileName = document.title,
                sessionId = sessionId,
                signTime = documentsGroup.date,
                signGroupId = signGroupId,
                signatoriesNames = documentsGroup.signatories,
                organization = document.organization,
                signedCms = document.signedCms ?: throw IllegalArgumentException(
                    "Document without a signature cannot be converted to a SignedDocumentEntity"
                )
            )

            signedDocumentDao.addSignedDocument(entity)
        }
    }

    override suspend fun getAllSignedDocumentsBySessionId(sessionId: Int): List<SignedDocumentsGroup> {
        val allSessionDocs = signedDocumentDao.getSignedDocumentsBySessionID(sessionId)
        if (allSessionDocs.isEmpty()) return emptyList()

        val groupedBySignGroup = allSessionDocs.groupBy { it.signGroupId }

        return groupedBySignGroup.map { (_, documentEntities) ->
            makeSignedDocumentsGroup(documentEntities)
        }
    }

    private fun makeSignedDocumentsGroup(documentEntities: List<SignedDocumentEntity>): SignedDocumentsGroup {
        val date = documentEntities.first().signTime

        val documents = documentEntities.map { entity ->
            Document(
                title = entity.fileName,
                date = date,
                organization = entity.organization,
                signedCms = entity.signedCms
            )
        }

        return SignedDocumentsGroup(
            documents = documents,
            date = date,
            signatories = documentEntities.first().signatoriesNames
        )
    }
}
