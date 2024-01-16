package ru.rutoken.tech.database

import androidx.room.Database
import androidx.room.RoomDatabase
import ru.rutoken.tech.database.document.DocumentDao
import ru.rutoken.tech.database.document.DocumentEntity
import ru.rutoken.tech.database.user.UserDao
import ru.rutoken.tech.database.user.UserEntity

@Database(entities = [UserEntity::class, DocumentEntity::class], version = 1)
abstract class Database : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun documentDao(): DocumentDao
}

const val AUTOGENERATE = 0
