package com.example.myappmobile.presentation.auth.register

import android.util.Log
import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myappmobile.core.di.AppContainer
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RegisterViewModel : ViewModel() {
    private val registerUseCase = AppContainer.registerUseCase
    private companion object {
        const val TAG = "REGISTER_DEBUG"
    }

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    private val registerExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Log.e(TAG, "Unhandled registration exception", throwable)
        _uiState.update {
            it.copy(
                isLoading = false,
                isRegisterSuccess = false,
                generalError = throwable.message ?: "Registration failed. Please try again."
            )
        }
    }

    fun onEvent(event: RegisterEvent) {
        when (event) {
            is RegisterEvent.FullNameChanged -> {
                _uiState.update {
                    it.copy(fullName = event.name, fullNameError = null, generalError = null)
                }
            }

            is RegisterEvent.EmailChanged -> {
                _uiState.update {
                    it.copy(email = event.email.trim(), emailError = null, generalError = null)
                }
            }

            is RegisterEvent.PasswordChanged -> {
                _uiState.update {
                    it.copy(password = event.password, passwordError = null, confirmPasswordError = null, generalError = null)
                }
            }

            is RegisterEvent.ConfirmPasswordChanged -> {
                _uiState.update {
                    it.copy(confirmPassword = event.confirmPassword, confirmPasswordError = null, generalError = null)
                }
            }

            is RegisterEvent.PhoneNumberChanged -> {
                val normalized = event.phoneNumber.filter { char -> char.isDigit() || char == '+' }
                _uiState.update {
                    it.copy(phoneNumber = normalized, phoneNumberError = null, generalError = null)
                }
            }

            is RegisterEvent.StoreNameChanged -> {
                _uiState.update {
                    it.copy(storeName = event.storeName, storeNameError = null, generalError = null)
                }
            }

            is RegisterEvent.AddressChanged -> {
                _uiState.update {
                    it.copy(address = event.address, addressError = null, generalError = null)
                }
            }

            is RegisterEvent.AccountTypeSelected -> {
                _uiState.update {
                    if (event.type == AccountType.BUYER) {
                        it.copy(
                            selectedAccountType = event.type,
                            address = "",
                            storeName = "",
                            addressError = null,
                            storeNameError = null,
                            generalError = null
                        )
                    } else {
                        it.copy(selectedAccountType = event.type, generalError = null)
                    }
                }
            }

            RegisterEvent.TogglePasswordVisibility -> {
                _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
            }

            RegisterEvent.ToggleConfirmPasswordVisibility -> {
                _uiState.update { it.copy(isConfirmPasswordVisible = !it.isConfirmPasswordVisible) }
            }

            RegisterEvent.Register -> {
                Log.d(TAG, "Registration requested")
                if (validateInputs()) {
                    performRegister()
                }
            }

            RegisterEvent.NavigateToLogin -> Unit

            RegisterEvent.ConsumeRegisterSuccess -> {
                _uiState.update { it.copy(isRegisterSuccess = false) }
            }

            RegisterEvent.ClearError -> {
                _uiState.update { it.copy(generalError = null) }
            }
        }
    }

    private fun validateInputs(): Boolean {
        val state = _uiState.value
        val phoneDigits = state.phoneNumber.filter(Char::isDigit)

        val fullNameError = when {
            state.fullName.isBlank() -> "Full name is required"
            state.fullName.trim().length < 2 -> "Enter your full name"
            else -> null
        }

        val emailError = when {
            state.email.isBlank() -> "Email is required"
            !Patterns.EMAIL_ADDRESS.matcher(state.email).matches() -> "Enter a valid email address"
            else -> null
        }

        val passwordError = when {
            state.password.isBlank() -> "Password is required"
            state.password.length < 8 -> "Password must be at least 8 characters"
            else -> null
        }

        val confirmPasswordError = when {
            state.confirmPassword.isBlank() -> "Confirm your password"
            state.confirmPassword != state.password -> "Passwords do not match"
            else -> null
        }

        val phoneNumberError = when {
            state.phoneNumber.isBlank() -> "Phone number is required"
            phoneDigits.length !in 8..15 -> "Enter a valid phone number"
            else -> null
        }

        val storeNameError = when {
            state.isArtisan && state.storeName.isBlank() -> "Store name is required"
            else -> null
        }

        val addressError = when {
            state.isArtisan && state.address.isBlank() -> "Address is required"
            else -> null
        }

        _uiState.update {
            it.copy(
                fullNameError = fullNameError,
                emailError = emailError,
                passwordError = passwordError,
                confirmPasswordError = confirmPasswordError,
                phoneNumberError = phoneNumberError,
                storeNameError = storeNameError,
                addressError = addressError
            )
        }

        val isValid = listOf(
            fullNameError,
            emailError,
            passwordError,
            confirmPasswordError,
            phoneNumberError,
            storeNameError,
            addressError
        ).all { error -> error == null }

        if (!isValid) {
            Log.d(TAG, "Registration validation failed")
        }

        return isValid
    }

    private fun performRegister() {
        viewModelScope.launch(registerExceptionHandler) {
            val state = _uiState.value
            Log.d(TAG, "Starting registration for type=${state.selectedAccountType}")
            _uiState.update { it.copy(isLoading = true, generalError = null) }
            try {
                delay(300)

                Log.d(
                    TAG,
                    "Registration payload prepared: email=${state.email}, phone=${state.phoneNumber}, store=${state.storeName.takeIf { it.isNotBlank() }}"
                )
                registerUseCase(
                    fullName = state.fullName,
                    email = state.email,
                    password = state.password,
                    phoneNumber = state.phoneNumber,
                    isSeller = state.isArtisan,
                    storeName = state.storeName,
                    address = state.address,
                ).onSuccess {
                    _uiState.update { current -> current.copy(isLoading = false, isRegisterSuccess = true) }
                }.onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isRegisterSuccess = false,
                            generalError = error.message ?: "Registration failed. Please try again."
                        )
                    }
                }
            } catch (exception: Exception) {
                Log.e(TAG, "Registration failed", exception)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isRegisterSuccess = false,
                        generalError = exception.message ?: "Registration failed. Please try again."
                    )
                }
            }
        }
    }
}
