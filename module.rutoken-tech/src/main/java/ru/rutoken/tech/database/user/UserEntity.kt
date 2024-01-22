package ru.rutoken.tech.database.user

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import ru.rutoken.tech.database.AUTOGENERATE

@Entity(
    tableName = "users",
    indices = [
        Index(value = ["certificate_der_value"], unique = true),
        Index(value = ["token_serial_number"], unique = true)
    ]
)
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = AUTOGENERATE,
    @ColumnInfo(name = "certificate_der_value") val certificateDerValue: ByteArray,
    @ColumnInfo(name = "cka_id") val ckaId: ByteArray,
    @ColumnInfo(name = "token_serial_number") val tokenSerialNumber: String
)