package com.example.myappmobile.presentation.auth.login

sealed class LoginEvent {
    data class EmailChanged(val email: String) : LoginEvent()
    data class PasswordChanged(val password: String) : LoginEvent()
    data object TogglePasswordVisibility : LoginEvent()
    data object SignIn : LoginEvent()
    data object SignInWithApple : LoginEvent()
    data object SignInWithGoogle : LoginEvent()
    data object NavigateToRegister : LoginEvent()
    data object NavigateToForgotPassword : LoginEvent()
    data object ConsumeLoginSuccess : LoginEvent()
    data class LoginFailed(val message: String) : LoginEvent()
    data object ClearError : LoginEvent()
}
