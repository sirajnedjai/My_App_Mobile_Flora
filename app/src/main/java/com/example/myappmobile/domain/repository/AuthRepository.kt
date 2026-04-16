package com.example.myappmobile.domain.repository

import com.example.myappmobile.domain.model.User
import kotlinx.coroutines.flow.StateFlow

interface AuthRepository {
    val currentUser: StateFlow<User>

    suspend fun login(email: String, password: String): Result<User>

    fun logout()

    suspend fun register(
        fullName: String,
        email: String,
        password: String,
        phoneNumber: String,
        isSeller: Boolean,
        storeName: String,
        address: String,
    ): Result<User>

    suspend fun updateCurrentUserProfile(
        fullName: String,
        email: String,
        phoneNumber: String,
    ): Result<User>

    suspend fun updateCurrentUserPassword(
        currentPassword: String,
        newPassword: String,
    ): Result<Unit>
}
