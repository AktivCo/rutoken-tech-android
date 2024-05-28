/*
 * Copyright (c) 2024, Aktiv-Soft JSC.
 * See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.tech.database.user

import androidx.room.Embedded
import androidx.room.Relation
import ru.rutoken.tech.database.document.DocumentEntity

data class UserWithDocuments(
    @Embedded val user: UserEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "owner_user_id"
    )
    val documents: List<DocumentEntity>
)