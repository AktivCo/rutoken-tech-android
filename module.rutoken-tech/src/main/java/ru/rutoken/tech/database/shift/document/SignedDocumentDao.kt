/*
 * Copyright (c) 2025, Aktiv-Soft JSC.
 * See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.tech.database.shift.document

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface SignedDocumentDao {
    @Query("SELECT * FROM signed_document WHERE session_id = :sessionId")
    suspend fun getSignedDocumentsBySessionID(sessionId: Int): List<SignedDocumentEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addSignedDocument(document: SignedDocumentEntity)
}