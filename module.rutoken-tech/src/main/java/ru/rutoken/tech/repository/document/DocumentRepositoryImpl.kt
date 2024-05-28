/*
 * Copyright (c) 2024, Aktiv-Soft JSC.
 * See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.tech.repository.document

import androidx.annotation.AnyThread
import ru.rutoken.tech.database.Database

@AnyThread
class DocumentRepositoryImpl(database: Database) : DocumentRepository {
    private val documentDao = database.documentDao()

    override suspend fun addDocument(document: Document) = documentDao.addDocument(document.documentEntity)
}