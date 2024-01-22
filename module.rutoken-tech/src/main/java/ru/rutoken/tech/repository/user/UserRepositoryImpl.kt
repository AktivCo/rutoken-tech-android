package ru.rutoken.tech.repository.user

import androidx.annotation.AnyThread
import androidx.annotation.MainThread
import androidx.lifecycle.map
import ru.rutoken.tech.database.Database

@AnyThread
class UserRepositoryImpl(database: Database) : UserRepository {
    private val userDao = database.userDao()

    override suspend fun getUser(userId: Int) = makeUser(userDao.getUser(userId))

    override suspend fun getUsers() = userDao.getUsers().map { makeUser(it) }

    @MainThread
    override fun getUsersAsync() = userDao.getUsersAsync().map { userEntityList ->
        userEntityList.map { makeUser(it) }
    }

    override suspend fun addUser(user: User) = userDao.addUser(user.userEntity)

    override suspend fun removeUser(user: User) = userDao.deleteUser(user.userEntity)
}
