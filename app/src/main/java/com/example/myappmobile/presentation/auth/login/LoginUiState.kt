package com.example.myappmobile.presentation.auth.login

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val isLoginSuccess: Boolean = false,
    val authenticatedRole: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val generalError: String? = null
) {
    val isFormValid: Boolean
        get() = email.isNotBlank() && password.isNotBlank()
}
