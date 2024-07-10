/*
 * Copyright (c) 2024, Aktiv-Soft JSC.
 * See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.tech.database

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.kotest.assertions.withClue
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import ru.rutoken.tech.database.user.UserDao
import ru.rutoken.tech.database.user.UserEntity

@RunWith(AndroidJUnit4::class)
class UserEntityReadWriteTest {
    private lateinit var userDao: UserDao
    private lateinit var db: Database

    @Before
    fun createDb() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            Database::class.java
        ).build()
        userDao = db.userDao()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun writeUser() = runTest {
        val user = UserEntity(
            certificateDerValue = byteArrayOf(),
            ckaId = byteArrayOf(),
            tokenSerialNumber = "",
            isBiometryActive = true,
            encryptedPin = byteArrayOf(1, 2, 3),
            cipherIv = byteArrayOf(2, 4, 6)
        )
        userDao.addUser(user)
        withClue("Should be 1 user") { userDao.getUsers().size shouldBe 1 }
    }
}