package com.example.myappmobile.presentation.checkout

import com.example.myappmobile.domain.model.CartItem

data class CheckoutUiState(
    val fullName: String = "",
    val phoneNumber: String = "",
    val state: String = "",
    val municipality: String = "",
    val neighborhood: String = "",
    val streetAddress: String = "",
    val country: String = "",
    val postalCode: String = "",
    val shippingMethod: String = "home_delivery",
    val paymentMethod: String = "card",
    val cardNumber: String = "",
    val cardName: String = "",
    val expiryDate: String = "",
    val cvv: String = "",
    val items: List<CartItem> = emptyList(),
    val subtotal: Double = 0.0,
    val shippingCost: Double = 0.0,
    val tax: Double = 0.0,
    val total: Double = 0.0,
    val isPlacingOrder: Boolean = false,
    val fullNameError: String? = null,
    val phoneNumberError: String? = null,
    val stateError: String? = null,
    val municipalityError: String? = null,
    val neighborhoodError: String? = null,
    val streetAddressError: String? = null,
    val countryError: String? = null,
    val postalCodeError: String? = null,
    val errorMessage: String? = null,
) {
    val itemCount: Int
        get() = items.sumOf { it.quantity }

    val isShippingValid: Boolean
        get() = fullName.isNotBlank() &&
            phoneNumber.isNotBlank() &&
            state.isNotBlank() &&
            municipality.isNotBlank() &&
            neighborhood.isNotBlank() &&
            streetAddress.isNotBlank() &&
            country.isNotBlank() &&
            postalCode.isNotBlank()

    val isPaymentValid: Boolean
        get() = when (paymentMethod) {
            "card" -> cardNumber.length >= 8 && cardName.isNotBlank() && expiryDate.length >= 4 && cvv.length >= 3
            else -> true
        }

    val canPlaceOrder: Boolean
        get() = items.isNotEmpty() && isShippingValid && isPaymentValid && !isPlacingOrder
}

data class ShippingOptionUi(
    val id: String,
    val title: String,
    val subtitle: String,
    val priceLabel: String,
    val priceValue: Double,
)

data class PaymentOptionUi(
    val id: String,
    val backendValue: String,
    val title: String,
)
