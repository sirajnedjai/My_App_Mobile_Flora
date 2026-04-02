package com.example.myappmobile.presentation.auth.verification

data class VerificationUiState(
    val code: String = "",
    val codeError: String? = null,
    val isVerified: Boolean = false
)
