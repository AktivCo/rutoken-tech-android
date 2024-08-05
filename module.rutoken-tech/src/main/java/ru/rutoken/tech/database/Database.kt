/*
 * Copyright (c) 2024, Aktiv-Soft JSC.
 * See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.tech.database

import androidx.room.Database
import androidx.room.RoomDatabase
import ru.rutoken.tech.database.bank.BankUserDao
import ru.rutoken.tech.database.bank.BankUserEntity
import ru.rutoken.tech.database.shift.ShiftUserDao
import ru.rutoken.tech.database.shift.ShiftUserEntity

@Database(entities = [BankUserEntity::class, ShiftUserEntity::class], version = 1)
abstract class Database : RoomDatabase() {
    abstract fun bankUserDao(): BankUserDao

    abstract fun shiftUserDao(): ShiftUserDao
}

const val AUTOGENERATE = 0
