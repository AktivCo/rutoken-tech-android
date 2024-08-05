/*
 * Copyright (c) 2024, Aktiv-Soft JSC.
 * See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.tech.repository.bank

import androidx.annotation.AnyThread
import ru.rutoken.tech.database.Database

@AnyThread
class BankUserRepositoryImpl(database: Database) : BankUserRepository {
    private val userDao = database.bankUserDao()

    override suspend fun getUser(userId: Int) = makeBankUser(userDao.getUser(userId))

    override suspend fun findUser(certificateDerValue: ByteArray): BankUser? {
        val foundUser = userDao.findUser(certificateDerValue)
        return if (foundUser == null) null else makeBankUser(foundUser)
    }

    override suspend fun getUsers() = userDao.getUsers().map { makeBankUser(it) }

    override suspend fun addUser(user: BankUser) = userDao.addUser(user.userEntity)

    override suspend fun deleteAllUsers() = userDao.deleteAllUsers()

    override suspend fun deleteUserEncryptedPin(userId: Int) {
        userDao.updateEncryptedInfo(userId, null, null)
    }

    override suspend fun updateUserEncryptedPin(userId: Int, encryptedPin: ByteArray, cipherIv: ByteArray) {
        userDao.updateEncryptedInfo(userId, encryptedPin, cipherIv)
    }
}
