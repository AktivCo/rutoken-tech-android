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
import ru.rutoken.tech.database.document.DocumentDao
import ru.rutoken.tech.database.document.DocumentEntity
import ru.rutoken.tech.database.user.UserDao
import ru.rutoken.tech.database.user.UserEntity

@RunWith(AndroidJUnit4::class)
class UserEntityReadWriteTest {
    private lateinit var userDao: UserDao
    private lateinit var documentDao: DocumentDao
    private lateinit var db: Database

    @Before
    fun createDb() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            Database::class.java
        ).build()
        userDao = db.userDao()
        documentDao = db.documentDao()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun writeUserAndReadRelatedDocumentsList() = runTest {
        val user = UserEntity(certificateDerValue = byteArrayOf(), ckaId = byteArrayOf(), tokenSerialNumber = "")
        userDao.addUser(user)
        val createdUser = userDao.getUsers().also {
            withClue("Should be 1 user") { it.size shouldBe 1 }
        }.first()
        val document = DocumentEntity(ownerUserId = createdUser.id)
        documentDao.addDocument(document)
        val userWithDocuments = userDao.getUserWithDocuments(createdUser.id)
        withClue("User should have 1 document") { userWithDocuments.documents.size shouldBe 1 }
        withClue("User's document owner id should be equal to user id") {
            userWithDocuments.documents.first().ownerUserId shouldBe createdUser.id
        }
    }
}