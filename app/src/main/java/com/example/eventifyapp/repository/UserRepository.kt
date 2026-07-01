package com.example.eventifyapp.repository

import com.example.eventifyapp.dao.UserDao
import com.example.eventifyapp.model.User
import kotlinx.coroutines.flow.Flow

class UserRepository(private val userDao: UserDao) {
    fun getUser(): Flow<User?> = userDao.getUser()

    fun getUserByEmailFlow(email: String): Flow<User?> = userDao.getUserByEmailFlow(email)

    suspend fun getUserByEmail(email: String): User? = userDao.getUserByEmail(email)

    suspend fun insertUser(user: User) {
        userDao.insertUser(user)
    }

    suspend fun updateUser(user: User) {
        userDao.updateUser(user)
    }
}

