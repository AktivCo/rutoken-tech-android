/*
 * Copyright (c) 2024, Aktiv-Soft JSC.
 * See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.tech.database

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import ru.rutoken.tech.database.bank.BankUserDao
import ru.rutoken.tech.database.bank.BankUserEntity
import ru.rutoken.tech.database.shift.document.SignedDocumentDao
import ru.rutoken.tech.database.shift.document.SignedDocumentEntity
import ru.rutoken.tech.database.shift.ShiftUserDao
import ru.rutoken.tech.database.shift.ShiftUserEntity

@Database(
    entities = [BankUserEntity::class, ShiftUserEntity::class, SignedDocumentEntity::class],
    version = 2,
    autoMigrations = [AutoMigration(from = 1, to = 2)]
)
@TypeConverters(Converters::class)
abstract class Database : RoomDatabase() {
    abstract fun bankUserDao(): BankUserDao

    abstract fun shiftUserDao(): ShiftUserDao

    abstract fun signedDocumentDao(): SignedDocumentDao
}

const val AUTOGENERATE = 0
