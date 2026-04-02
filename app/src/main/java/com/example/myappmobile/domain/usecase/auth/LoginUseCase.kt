package com.example.myappmobile.domain.usecase.auth

import com.example.myappmobile.domain.repository.AuthRepository

class LoginUseCase(private val authRepository: AuthRepository) {
    suspend operator fun invoke(email: String, password: String) =
        authRepository.login(email, password)
}
