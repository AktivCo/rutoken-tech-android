/*
 * Copyright (c) 2024, Aktiv-Soft JSC.
 * See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.tech.database.user

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import ru.rutoken.tech.database.AUTOGENERATE

@Entity(
    tableName = "users",
    indices = [
        Index(value = ["certificate_der_value"], unique = true)
    ]
)
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = AUTOGENERATE,
    @ColumnInfo(name = "certificate_der_value") val certificateDerValue: ByteArray,
    @ColumnInfo(name = "cka_id") val ckaId: ByteArray,
    @ColumnInfo(name = "token_serial_number") val tokenSerialNumber: String,
    @ColumnInfo(name = "is_biometry_active") val isBiometryActive: Boolean,
    @ColumnInfo(name = "encrypted_pin") val encryptedPin: ByteArray?,
    @ColumnInfo(name = "cipher_iv") val cipherIv: ByteArray?
)