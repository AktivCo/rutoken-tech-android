/*
 * Copyright (c) 2024, Aktiv-Soft JSC.
 * See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.tech.database.user

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUser(userId: Int): UserEntity

    @Query("SELECT * FROM users")
    suspend fun getUsers(): List<UserEntity>

    @Query("SELECT * FROM users")
    fun getUsersAsync(): LiveData<List<UserEntity>>

    @Transaction
    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUserWithDocuments(userId: Int): UserWithDocuments

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun addUser(user: UserEntity)

    @Delete
    suspend fun deleteUser(user: UserEntity)
}