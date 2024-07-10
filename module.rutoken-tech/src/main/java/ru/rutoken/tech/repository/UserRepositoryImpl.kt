/*
 * Copyright (c) 2024, Aktiv-Soft JSC.
 * See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.tech.repository

import androidx.annotation.AnyThread
import androidx.annotation.MainThread
import androidx.lifecycle.map
import ru.rutoken.tech.database.Database

@AnyThread
class UserRepositoryImpl(database: Database) : UserRepository {
    private val userDao = database.userDao()

    override suspend fun getUser(userId: Int) = makeUser(userDao.getUser(userId))

    override suspend fun findUser(certificateDerValue: ByteArray): User? {
        val foundUser = userDao.findUser(certificateDerValue)
        return if (foundUser == null) null else makeUser(foundUser)
    }

    override suspend fun getUsers() = userDao.getUsers().map { makeUser(it) }

    @MainThread
    override fun getUsersAsync() = userDao.getUsersAsync().map { userEntityList ->
        userEntityList.map { makeUser(it) }
    }

    override suspend fun addUser(user: User) = userDao.addUser(user.userEntity)

    override suspend fun deleteAllUsers() = userDao.deleteAllUsers()

    override suspend fun deleteUserEncryptedPin(userId: Int) {
        userDao.updateEncryptedInfo(userId, null, null)
    }

    override suspend fun updateUserEncryptedPin(userId: Int, encryptedPin: ByteArray, cipherIv: ByteArray) {
        userDao.updateEncryptedInfo(userId, encryptedPin, cipherIv)
    }
}
