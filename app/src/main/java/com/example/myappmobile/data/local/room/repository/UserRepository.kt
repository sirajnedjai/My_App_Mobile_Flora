package com.example.myappmobile.data.local.room.repository

import com.example.myappmobile.data.local.room.dao.UserDao
import com.example.myappmobile.data.local.room.entity.UserEntity
import kotlinx.coroutines.flow.Flow

class UserRepository(
    private val userDao: UserDao,
) {
    fun getAllUsers(): Flow<List<UserEntity>> = userDao.getAll()

    suspend fun getUserByEmail(email: String): UserEntity? = userDao.getByEmail(email)

    suspend fun getUserByCredentials(email: String, password: String): UserEntity? =
        userDao.getByEmailAndPassword(email, password)

    suspend fun insertUser(user: UserEntity): Long {
        return userDao.insert(user)
    }

    suspend fun deleteUser(user: UserEntity) {
        userDao.delete(user)
    }

    suspend fun deleteAllUsers() {
        userDao.deleteAll()
    }
}
