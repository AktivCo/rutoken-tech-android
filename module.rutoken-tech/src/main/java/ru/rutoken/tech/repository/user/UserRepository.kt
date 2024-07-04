/*
 * Copyright (c) 2024, Aktiv-Soft JSC.
 * See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.tech.repository.user

import androidx.lifecycle.LiveData

interface UserRepository {
    suspend fun getUser(userId: Int): User
    suspend fun findUser(certificateDerValue: ByteArray): User?
    suspend fun getUsers(): List<User>
    fun getUsersAsync(): LiveData<List<User>>
    suspend fun addUser(user: User)
    suspend fun removeUser(user: User)
}
