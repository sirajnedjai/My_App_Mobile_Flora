package com.example.myappmobile.presentation.checkout.address

data class AddressUiState(
    val fullName: String = "",
    val street: String = "",
    val city: String = "",
    val postalCode: String = "",
    val country: String = "",
) {
    val isValid: Boolean
        get() = fullName.isNotBlank() &&
            street.isNotBlank() &&
            city.isNotBlank() &&
            postalCode.isNotBlank() &&
            country.isNotBlank()
}
