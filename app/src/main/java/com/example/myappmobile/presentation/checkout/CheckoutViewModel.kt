package com.example.myappmobile.presentation.checkout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myappmobile.core.di.AppContainer
import com.example.myappmobile.domain.model.Address
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CheckoutViewModel : ViewModel() {

    private val orderRepository = AppContainer.orderRepository
    private val cartRepository = AppContainer.cartRepository
    private val createOrderUseCase = AppContainer.createOrderUseCase

    val shippingOptions = listOf(
        ShippingOptionUi("Standard Delivery", "Arrives in 4-6 business days", "$12", 12.0),
        ShippingOptionUi("Express Courier", "Arrives in 2-3 business days", "$18", 18.0),
        ShippingOptionUi("Concierge White-Glove", "Priority handling and presentation", "$25", 25.0),
    )

    val paymentOptions = listOf(
        PaymentOptionUi("Card", "Card"),
        PaymentOptionUi("PayPal", "PayPal"),
        PaymentOptionUi("Cash on Delivery", "Cash"),
    )

    private val formState = MutableStateFlow(CheckoutUiState())

    val uiState: StateFlow<CheckoutUiState> = combine(
        formState,
        cartRepository.cartItems,
        orderRepository.checkoutDraft,
    ) { form, cartItems, draft ->
        val shippingCost = shippingOptions.firstOrNull { it.title == form.shippingMethod }?.priceValue ?: 12.0
        val subtotal = cartItems.sumOf { it.product.price * it.quantity }
        val tax = subtotal * 0.08
        form.copy(
            fullName = if (form.fullName.isBlank()) draft.address?.fullName.orEmpty() else form.fullName,
            street = if (form.street.isBlank()) draft.address?.street.orEmpty() else form.street,
            city = if (form.city.isBlank()) draft.address?.city.orEmpty() else form.city,
            postalCode = if (form.postalCode.isBlank()) draft.address?.postalCode.orEmpty() else form.postalCode,
            country = if (form.country.isBlank()) draft.address?.country.orEmpty() else form.country,
            shippingMethod = form.shippingMethod.ifBlank { draft.shippingMethod },
            paymentMethod = form.paymentMethod.ifBlank { draft.paymentMethod.substringBefore(" ending") },
            items = cartItems,
            subtotal = subtotal,
            shippingCost = shippingCost,
            tax = tax,
            total = subtotal + shippingCost + tax,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = CheckoutUiState(
            shippingMethod = orderRepository.checkoutDraft.value.shippingMethod,
            paymentMethod = orderRepository.checkoutDraft.value.paymentMethod.substringBefore(" ending"),
        ),
    )

    fun onFullNameChange(value: String) = formState.update { it.copy(fullName = value, errorMessage = null) }
    fun onStreetChange(value: String) = formState.update { it.copy(street = value, errorMessage = null) }
    fun onCityChange(value: String) = formState.update { it.copy(city = value, errorMessage = null) }
    fun onPostalCodeChange(value: String) = formState.update { it.copy(postalCode = value, errorMessage = null) }
    fun onCountryChange(value: String) = formState.update { it.copy(country = value, errorMessage = null) }
    fun onShippingMethodSelected(value: String) = formState.update { it.copy(shippingMethod = value, errorMessage = null) }
    fun onPaymentMethodSelected(value: String) = formState.update { it.copy(paymentMethod = value, errorMessage = null) }
    fun onCardNumberChange(value: String) = formState.update { it.copy(cardNumber = value.take(19), errorMessage = null) }
    fun onCardNameChange(value: String) = formState.update { it.copy(cardName = value, errorMessage = null) }
    fun onExpiryDateChange(value: String) = formState.update { it.copy(expiryDate = value.take(5), errorMessage = null) }
    fun onCvvChange(value: String) = formState.update { it.copy(cvv = value.take(4), errorMessage = null) }

    fun placeOrder(onPlaced: () -> Unit) {
        val state = uiState.value
        val validationError = when {
            state.items.isEmpty() -> "Your cart is empty."
            !state.isAddressValid -> "Complete the shipping address before placing the order."
            state.paymentMethod == "Card" && state.cardNumber.length < 8 -> "Enter a valid card number."
            state.paymentMethod == "Card" && state.cardName.isBlank() -> "Enter the cardholder name."
            state.paymentMethod == "Card" && state.expiryDate.length < 4 -> "Enter the expiry date."
            state.paymentMethod == "Card" && state.cvv.length < 3 -> "Enter the CVV."
            else -> null
        }
        if (validationError != null) {
            formState.update { it.copy(errorMessage = validationError) }
            return
        }

        viewModelScope.launch {
            formState.update { it.copy(isPlacingOrder = true, errorMessage = null) }
            orderRepository.updateAddress(
                Address(
                    id = "checkout_address",
                    label = "Checkout",
                    fullName = state.fullName,
                    street = state.street,
                    city = state.city,
                    postalCode = state.postalCode,
                    country = state.country,
                    isPrimary = true,
                ),
            )
            orderRepository.updateShippingMethod(state.shippingMethod)
            orderRepository.updatePaymentMethod(
                if (state.paymentMethod == "Card") "Card ending in ${state.cardNumber.takeLast(4)}" else state.paymentMethod,
            )
            createOrderUseCase()
            formState.value = CheckoutUiState(
                shippingMethod = orderRepository.checkoutDraft.value.shippingMethod,
                paymentMethod = orderRepository.checkoutDraft.value.paymentMethod.substringBefore(" ending"),
            )
            onPlaced()
        }
    }
}
