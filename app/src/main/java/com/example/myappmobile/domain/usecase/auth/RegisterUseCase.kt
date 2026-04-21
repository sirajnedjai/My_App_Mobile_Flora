package com.example.myappmobile.domain.usecase.auth

import com.example.myappmobile.domain.repository.AuthRepository

class RegisterUseCase(private val authRepository: AuthRepository) {
    suspend operator fun invoke(
        fullName: String,
        email: String,
        password: String,
        phoneNumber: String,
        isSeller: Boolean,
        storeName: String,
        address: String,
        postalCode: String,
    ) = authRepository.register(
        fullName = fullName,
        email = email,
        password = password,
        phoneNumber = phoneNumber,
        isSeller = isSeller,
        storeName = storeName,
        address = address,
        postalCode = postalCode,
    )
}
