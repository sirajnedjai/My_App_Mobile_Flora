package com.example.myappmobile.presentation.checkout.address

data class AddressUiState(
    val fullName: String = "",
    val state: String = "",
    val municipality: String = "",
    val neighborhood: String = "",
    val streetAddress: String = "",
    val postalCode: String = "",
    val country: String = "",
) {
    val isValid: Boolean
        get() = fullName.isNotBlank() &&
            state.isNotBlank() &&
            municipality.isNotBlank() &&
            neighborhood.isNotBlank() &&
            streetAddress.isNotBlank() &&
            postalCode.isNotBlank() &&
            country.isNotBlank()
}
