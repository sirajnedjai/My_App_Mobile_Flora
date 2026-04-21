package com.example.myappmobile.presentation.auth.register

import android.util.Patterns

data class RegisterUiState(
    val fullName: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val phoneNumber: String = "",
    val address: String = "",
    val postalCode: String = "",
    val storeName: String = "",
    val isPasswordVisible: Boolean = false,
    val isConfirmPasswordVisible: Boolean = false,
    val selectedAccountType: AccountType = AccountType.BUYER,
    val isLoading: Boolean = false,
    val isRegisterSuccess: Boolean = false,
    val successMessage: String? = null,
    val fullNameError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
    val phoneNumberError: String? = null,
    val addressError: String? = null,
    val postalCodeError: String? = null,
    val storeNameError: String? = null,
    val generalError: String? = null,
) {
    val isArtisan: Boolean
        get() = selectedAccountType == AccountType.SELLER

    val isFormValid: Boolean
        get() {
            val phoneDigits = phoneNumber.filter(Char::isDigit)
            val baseValid = fullName.trim().length >= 2 &&
                Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches() &&
                password.length >= 8 &&
                password == confirmPassword &&
                phoneDigits.length in 8..15

            return if (isArtisan) {
                baseValid && address.isNotBlank() && postalCode.isNotBlank() && storeName.isNotBlank()
            } else {
                baseValid
            }
        }
}

enum class AccountType(val displayName: String, val subtitle: String) {
    BUYER("Buyer", "Discover unique handmade products, save favorites, place orders, and enjoy a smooth shopping experience on FLORA."),
    SELLER("Seller", "Create your artisan profile, showcase your handmade products, manage orders, and grow your business through FLORA.")
}
