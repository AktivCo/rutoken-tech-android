/*
 * Copyright (c) 2024, Aktiv-Soft JSC.
 * See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.tech.repository.shift

import androidx.annotation.AnyThread
import ru.rutoken.tech.database.Database

@AnyThread
class ShiftUserRepositoryImpl(database: Database) : ShiftUserRepository {
    private val userDao = database.shiftUserDao()

    override suspend fun getUser(userId: Int) = makeShiftUser(userDao.getUser(userId))

    override suspend fun findUser(certificateDerValue: ByteArray): ShiftUser? {
        val foundUser = userDao.findUser(certificateDerValue)
        return if (foundUser == null) null else makeShiftUser(foundUser)
    }

    override suspend fun getUsers() = userDao.getUsers().map { makeShiftUser(it) }

    override suspend fun addUser(user: ShiftUser) = userDao.addUser(user.userEntity)

    override suspend fun deleteAllUsers() = userDao.deleteAllUsers()
}
