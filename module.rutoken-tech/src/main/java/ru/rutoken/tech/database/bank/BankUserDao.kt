/*
 * Copyright (c) 2024, Aktiv-Soft JSC.
 * See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.tech.database.bank

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface BankUserDao {
    @Query("SELECT * FROM bank_users WHERE id = :userId")
    suspend fun getUser(userId: Int): BankUserEntity

    @Query("SELECT * FROM bank_users WHERE certificate_der_value = :certificateDerValue")
    suspend fun findUser(certificateDerValue: ByteArray): BankUserEntity?

    @Query("SELECT * FROM bank_users")
    suspend fun getUsers(): List<BankUserEntity>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun addUser(user: BankUserEntity)

    @Query("DELETE FROM bank_users")
    suspend fun deleteAllUsers()

    @Query("UPDATE bank_users SET encrypted_pin = :encryptedPin, cipher_iv = :cipherIv WHERE id = :userId")
    suspend fun updateEncryptedInfo(userId: Int, encryptedPin: ByteArray?, cipherIv: ByteArray?)
}