package ru.rutoken.tech.repository.user

import androidx.lifecycle.LiveData

interface UserRepository {
    suspend fun getUser(userId: Int): User
    suspend fun getUsers(): List<User>
    fun getUsersAsync(): LiveData<List<User>>
    suspend fun addUser(user: User)
    suspend fun removeUser(user: User)
}
