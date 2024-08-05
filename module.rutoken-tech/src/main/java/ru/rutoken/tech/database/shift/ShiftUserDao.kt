/*
 * Copyright (c) 2024, Aktiv-Soft JSC.
 * See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.tech.database.shift

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ShiftUserDao {
    @Query("SELECT * FROM shift_users WHERE id = :userId")
    suspend fun getUser(userId: Int): ShiftUserEntity

    @Query("SELECT * FROM shift_users WHERE certificate_der_value = :certificateDerValue")
    suspend fun findUser(certificateDerValue: ByteArray): ShiftUserEntity?

    @Query("SELECT * FROM shift_users")
    suspend fun getUsers(): List<ShiftUserEntity>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun addUser(user: ShiftUserEntity)

    @Query("DELETE FROM shift_users")
    suspend fun deleteAllUsers()
}