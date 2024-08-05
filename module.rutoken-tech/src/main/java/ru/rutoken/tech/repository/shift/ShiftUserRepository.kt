/*
 * Copyright (c) 2024, Aktiv-Soft JSC.
 * See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.tech.repository.shift

interface ShiftUserRepository {
    suspend fun getUser(userId: Int): ShiftUser
    suspend fun findUser(certificateDerValue: ByteArray): ShiftUser?
    suspend fun getUsers(): List<ShiftUser>
    suspend fun addUser(user: ShiftUser)
    suspend fun deleteAllUsers()
}
