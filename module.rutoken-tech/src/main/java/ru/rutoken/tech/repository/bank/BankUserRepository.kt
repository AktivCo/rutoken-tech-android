/*
 * Copyright (c) 2024, Aktiv-Soft JSC.
 * See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.tech.repository.bank

interface BankUserRepository {
    suspend fun getUser(userId: Int): BankUser
    suspend fun findUser(certificateDerValue: ByteArray): BankUser?
    suspend fun getUsers(): List<BankUser>
    suspend fun addUser(user: BankUser)
    suspend fun deleteAllUsers()
    suspend fun deleteUserEncryptedPin(userId: Int)
    suspend fun updateUserEncryptedPin(userId: Int, encryptedPin: ByteArray, cipherIv: ByteArray)
}
