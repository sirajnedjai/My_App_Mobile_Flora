package com.example.myappmobile.presentation.auth.forgotpassword

data class ForgotPasswordUiState(
    val email: String = "",
    val message: String? = null,
) {
    val isValid: Boolean
        get() = email.isNotBlank()
}
