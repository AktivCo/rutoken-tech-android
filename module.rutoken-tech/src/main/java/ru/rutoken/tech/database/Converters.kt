/*
 * Copyright (c) 2025, Aktiv-Soft JSC.
 * See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.tech.database

import androidx.room.TypeConverter
import java.time.LocalDate

class Converters {
    @TypeConverter
    fun fromLongToLocalDate(value: Long): LocalDate {
        return LocalDate.ofEpochDay(value)
    }

    @TypeConverter
    fun fromLocalDateToLong(date: LocalDate): Long {
        return date.toEpochDay()
    }

    @TypeConverter
    fun fromListToString(value: List<String>): String {
        return value.joinToString(separator = ",")
    }

    @TypeConverter
    fun fromStringToList(value: String): List<String> {
        return if (value.isNotEmpty()) value.split(",") else emptyList()
    }
}