/*
 * Copyright (c) 2025, Aktiv-Soft JSC.
 * See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.tech.database.shift.document

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import ru.rutoken.tech.database.AUTOGENERATE
import ru.rutoken.tech.database.shift.ShiftUserEntity
import java.time.LocalDate

@Entity(
    tableName = "signed_document",
    foreignKeys = [ForeignKey(
        entity = ShiftUserEntity::class,
        parentColumns = arrayOf("id"),
        childColumns = arrayOf("session_id"),
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["sign_group_id", "session_id", "file_name"], unique = true)],
)
data class SignedDocumentEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = AUTOGENERATE,
    @ColumnInfo(name = "file_name") val fileName: String,
    @ColumnInfo(name = "session_id") val sessionId: Int,
    @ColumnInfo(name = "sign_time") val signTime: LocalDate,
    @ColumnInfo(name = "sign_group_id") val signGroupId: String,
    val organization: String,
    @ColumnInfo(name = "signatories_names") val signatoriesNames: List<String>,
    @ColumnInfo(name = "signed_cms", typeAffinity = ColumnInfo.BLOB) val signedCms: ByteArray,
)
