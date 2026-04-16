package com.example.myappmobile.presentation.auth.login

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myappmobile.core.di.AppContainer
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {
    private val loginUseCase = AppContainer.loginUseCase
    private companion object {
        const val TAG = "LOGIN_DEBUG"
    }

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val loginExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Log.e(TAG, "Unhandled login coroutine exception", throwable)
        _uiState.update {
            it.copy(
                isLoading = false,
                isLoginSuccess = false,
                generalError = throwable.message ?: "Login failed. Please try again."
            )
        }
    }

    fun onEvent(event: LoginEvent) {
        when (event) {
            is LoginEvent.EmailChanged -> {
                Log.d(TAG, "Email changed")
                _uiState.update {
                    it.copy(
                        email = event.email.trim(),
                        emailError = null,
                        generalError = null
                    )
                }
            }

            is LoginEvent.PasswordChanged -> {
                Log.d(TAG, "Password changed")
                _uiState.update {
                    it.copy(
                        password = event.password,
                        passwordError = null,
                        generalError = null
                    )
                }
            }

            LoginEvent.TogglePasswordVisibility -> {
                Log.d(TAG, "Password visibility toggled")
                _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
            }

            LoginEvent.SignIn -> {
                Log.d(TAG, "Sign in requested")
                if (validateInputs()) {
                    performLogin()
                }
            }

            LoginEvent.SignInWithApple -> {
                // TODO: Integrate Apple Sign In
            }

            LoginEvent.SignInWithGoogle -> {
                // TODO: Integrate Google Sign In
            }

            LoginEvent.NavigateToRegister -> {
                // Navigation handled in the composable
            }

            LoginEvent.NavigateToForgotPassword -> {
                // Navigation handled in the composable
            }

            LoginEvent.ConsumeLoginSuccess -> {
                Log.d(TAG, "Login success consumed")
                _uiState.update { it.copy(isLoginSuccess = false, authenticatedRole = null) }
            }

            is LoginEvent.LoginFailed -> {
                Log.e(TAG, "Login flow failed: ${event.message}")
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isLoginSuccess = false,
                        generalError = event.message
                    )
                }
            }

            LoginEvent.ClearError -> {
                _uiState.update { it.copy(generalError = null) }
            }
        }
    }

    private fun validateInputs(): Boolean {
        val state = _uiState.value
        var isValid = true
        Log.d(TAG, "Validating inputs")

        if (state.email.isBlank()) {
            _uiState.update { it.copy(emailError = "Email is required") }
            isValid = false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(state.email).matches()) {
            _uiState.update { it.copy(emailError = "Enter a valid email address") }
            isValid = false
        }

        if (state.password.isBlank()) {
            _uiState.update { it.copy(passwordError = "Password is required") }
            isValid = false
        } else if (state.password.length < 6) {
            _uiState.update { it.copy(passwordError = "Password must be at least 6 characters") }
            isValid = false
        }

        Log.d(TAG, "Validation result: $isValid")
        return isValid
    }

    private fun performLogin() {
        viewModelScope.launch(loginExceptionHandler) {
            Log.d(TAG, "Performing login")
            _uiState.update { it.copy(isLoading = true, generalError = null) }
            try {
                delay(300)
                loginUseCase(_uiState.value.email, _uiState.value.password)
                    .onSuccess {
                        Log.d(TAG, "Login successful")
                        _uiState.update { state ->
                            state.copy(
                                isLoading = false,
                                isLoginSuccess = true,
                                authenticatedRole = if (it.isSeller) "Seller" else "Customer",
                            )
                        }
                    }
                    .onFailure { error ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                isLoginSuccess = false,
                                authenticatedRole = null,
                                generalError = error.message ?: "Login failed. Please try again."
                            )
                        }
                    }
            } catch (e: Exception) {
                Log.e(TAG, "Login failed", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isLoginSuccess = false,
                        authenticatedRole = null,
                        generalError = e.message ?: "Login failed. Please try again."
                    )
                }
            }
        }
    }
}
