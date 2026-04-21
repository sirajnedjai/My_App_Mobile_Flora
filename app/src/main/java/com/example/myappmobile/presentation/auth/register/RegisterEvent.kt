package com.example.myappmobile.presentation.auth.register

sealed class RegisterEvent {
    data class FullNameChanged(val name: String) : RegisterEvent()
    data class EmailChanged(val email: String) : RegisterEvent()
    data class PasswordChanged(val password: String) : RegisterEvent()
    data class ConfirmPasswordChanged(val confirmPassword: String) : RegisterEvent()
    data class PhoneNumberChanged(val phoneNumber: String) : RegisterEvent()
    data class StoreNameChanged(val storeName: String) : RegisterEvent()
    data class AddressChanged(val address: String) : RegisterEvent()
    data class PostalCodeChanged(val postalCode: String) : RegisterEvent()
    data class AccountTypeSelected(val type: AccountType) : RegisterEvent()
    data object TogglePasswordVisibility : RegisterEvent()
    data object ToggleConfirmPasswordVisibility : RegisterEvent()
    data object Register : RegisterEvent()
    data object NavigateToLogin : RegisterEvent()
    data object ConsumeRegisterSuccess : RegisterEvent()
    data object ClearError : RegisterEvent()
}
