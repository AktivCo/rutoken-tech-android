/*
 * Copyright (c) 2024, Aktiv-Soft JSC.
 * See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.tech.database.bank

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import io.kotest.assertions.withClue
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import ru.rutoken.tech.database.Database

class BankUserEntityReadWriteTest {
    private lateinit var userDao: BankUserDao
    private lateinit var db: Database

    @Before
    fun createDb() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            Database::class.java
        ).build()
        userDao = db.bankUserDao()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun writeUser() = runTest {
        val user = BankUserEntity(
            certificateDerValue = CERTIFICATE_DER_VALUE,
            ckaId = CKA_ID,
            tokenSerialNumber = TOKEN_SERIAL_NUMBER,
            isBiometryActive = IS_BIOMETRY_ACTIVE,
            encryptedPin = ENCRYPTED_PIN,
            cipherIv = CIPHER_IV
        )
        userDao.addUser(user)
        withClue("Should be 1 user") { userDao.getUsers().size shouldBe 1 }

        withClue("Bank user fields should be correct") {
            val entity = userDao.getUser(ID)
            entity.certificateDerValue shouldBe CERTIFICATE_DER_VALUE
            entity.ckaId shouldBe CKA_ID
            entity.tokenSerialNumber shouldBe TOKEN_SERIAL_NUMBER
            entity.isBiometryActive shouldBe IS_BIOMETRY_ACTIVE
            entity.encryptedPin shouldBe ENCRYPTED_PIN
            entity.cipherIv shouldBe CIPHER_IV
        }
    }

    companion object {
        private const val ID = 1
        private val CERTIFICATE_DER_VALUE = "CERTIFICATE_DER_VALUE".encodeToByteArray()
        private val CKA_ID = "CKA_ID".encodeToByteArray()
        private const val TOKEN_SERIAL_NUMBER = "TOKEN_SERIAL_NUMBER"
        private const val IS_BIOMETRY_ACTIVE = true
        private val ENCRYPTED_PIN = byteArrayOf(1, 2, 3)
        private val CIPHER_IV = byteArrayOf(2, 4, 6)
    }
}