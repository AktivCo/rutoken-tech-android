package ru.rutoken.tech.database.document

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.rutoken.tech.database.AUTOGENERATE

@Entity(
    tableName = "documents"
)
data class DocumentEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = AUTOGENERATE,
    @ColumnInfo(name = "owner_user_id") val ownerUserId: Int
)