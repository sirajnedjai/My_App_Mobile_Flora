package com.example.myappmobile.data.repository

import com.example.myappmobile.data.local.dummy.DummyUsers
import com.example.myappmobile.domain.model.User
import com.example.myappmobile.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AuthRepositoryImpl : AuthRepository {

    private val _currentUser = MutableStateFlow(DummyUsers.buyer)
    override val currentUser: StateFlow<User> = _currentUser.asStateFlow()

    override suspend fun login(email: String, password: String): Result<User> {
        val normalizedEmail = email.trim().lowercase()
        val user = when {
            "sienna" in normalizedEmail || "seller" in normalizedEmail -> DummyUsers.seller
            "elena" in normalizedEmail -> DummyUsers.elena
            "julianne" in normalizedEmail -> DummyUsers.buyer2
            else -> DummyUsers.buyer
        }.copy(email = email.trim(), isAuthenticated = true)

        _currentUser.value = user
        return Result.success(user)
    }

    override suspend fun register(
        fullName: String,
        email: String,
        password: String,
        phoneNumber: String,
        isSeller: Boolean,
        storeName: String,
        address: String,
    ): Result<User> {
        val user = User(
            id = if (isSeller) "new_seller" else "new_buyer",
            fullName = fullName.trim(),
            email = email.trim(),
            phone = phoneNumber.trim(),
            membershipTier = if (isSeller) "MASTER CERAMICIST" else "PREMIUM MEMBER",
            isAuthenticated = true,
            isSeller = isSeller,
        )
        _currentUser.value = user
        return Result.success(user)
    }
}
