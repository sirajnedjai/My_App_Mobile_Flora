package com.example.myappmobile.presentation.checkout

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myappmobile.core.di.AppContainer
import com.example.myappmobile.data.remote.toApiException
import com.example.myappmobile.domain.model.Address
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CheckoutViewModel : ViewModel() {
    private companion object {
        const val TAG = "CheckoutViewModel"
    }

    private val orderRepository = AppContainer.orderRepository
    private val cartRepository = AppContainer.cartRepository
    private val createOrderUseCase = AppContainer.createOrderUseCase

    val shippingOptions = listOf(
        ShippingOptionUi("home_delivery", "Home Delivery", "Delivered directly to your shipping address.", "300", 300.0),
        ShippingOptionUi("office_pickup", "Office Pickup", "Collect your order from the seller pickup point.", "150", 150.0),
    )

    val paymentOptions = listOf(
        PaymentOptionUi("card", "card", "Card"),
        PaymentOptionUi("cash_on_delivery", "cash_on_delivery", "Cash on Delivery"),
    )

    private val formState = MutableStateFlow(CheckoutUiState())

    val uiState: StateFlow<CheckoutUiState> = combine(
        formState,
        cartRepository.cartItems,
        orderRepository.checkoutDraft,
    ) { form, cartItems, draft ->
        val shippingCost = shippingOptions.firstOrNull { it.id == form.shippingMethod }?.priceValue ?: 12.0
        val subtotal = cartItems.sumOf { it.product.price * it.quantity }
        val tax = subtotal * 0.08
        form.copy(
            fullName = if (form.fullName.isBlank()) draft.address?.fullName.orEmpty() else form.fullName,
            phoneNumber = if (form.phoneNumber.isBlank()) draft.address?.phoneNumber.orEmpty().ifBlank { AppContainer.authRepository.currentUser.value.phone } else form.phoneNumber,
            state = if (form.state.isBlank()) draft.address?.state.orEmpty() else form.state,
            municipality = if (form.municipality.isBlank()) draft.address?.municipality.orEmpty() else form.municipality,
            neighborhood = if (form.neighborhood.isBlank()) draft.address?.neighborhood.orEmpty() else form.neighborhood,
            streetAddress = if (form.streetAddress.isBlank()) draft.address?.street.orEmpty() else form.streetAddress,
            country = if (form.country.isBlank()) draft.address?.country.orEmpty() else form.country,
            postalCode = if (form.postalCode.isBlank()) draft.address?.postalCode.orEmpty() else form.postalCode,
            shippingMethod = form.shippingMethod.ifBlank { draft.shippingMethod },
            paymentMethod = form.paymentMethod.ifBlank { draft.paymentMethod },
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
            paymentMethod = orderRepository.checkoutDraft.value.paymentMethod,
        ),
    )

    fun onFullNameChange(value: String) = formState.update { it.copy(fullName = value, fullNameError = null, errorMessage = null) }
    fun onPhoneNumberChange(value: String) = formState.update { it.copy(phoneNumber = value, phoneNumberError = null, errorMessage = null) }
    fun onStateChange(value: String) = formState.update { it.copy(state = value, stateError = null, errorMessage = null) }
    fun onMunicipalityChange(value: String) = formState.update { it.copy(municipality = value, municipalityError = null, errorMessage = null) }
    fun onNeighborhoodChange(value: String) = formState.update { it.copy(neighborhood = value, neighborhoodError = null, errorMessage = null) }
    fun onStreetAddressChange(value: String) = formState.update { it.copy(streetAddress = value, streetAddressError = null, errorMessage = null) }
    fun onCountryChange(value: String) = formState.update { it.copy(country = value, countryError = null, errorMessage = null) }
    fun onPostalCodeChange(value: String) = formState.update { it.copy(postalCode = value, postalCodeError = null, errorMessage = null) }
    fun onShippingMethodSelected(value: String) = formState.update { it.copy(shippingMethod = value, errorMessage = null) }
    fun onPaymentMethodSelected(value: String) = formState.update { it.copy(paymentMethod = value, errorMessage = null) }
    fun onCardNumberChange(value: String) = formState.update { it.copy(cardNumber = value.take(19), errorMessage = null) }
    fun onCardNameChange(value: String) = formState.update { it.copy(cardName = value, errorMessage = null) }
    fun onExpiryDateChange(value: String) = formState.update { it.copy(expiryDate = value.take(5), errorMessage = null) }
    fun onCvvChange(value: String) = formState.update { it.copy(cvv = value.take(4), errorMessage = null) }

    fun placeOrder(onPlaced: () -> Unit) {
        val state = uiState.value
        val fullNameError = state.fullName.takeIf { it.isBlank() }?.let { "Full name is required." }
        val phoneNumberError = when {
            state.phoneNumber.isBlank() -> "Phone number is required."
            state.phoneNumber.length < 8 -> "Enter a valid phone number."
            else -> null
        }
        val stateError = state.state.takeIf { it.isBlank() }?.let { "State is required." }
        val municipalityError = state.municipality.takeIf { it.isBlank() }?.let { "Municipality is required." }
        val neighborhoodError = state.neighborhood.takeIf { it.isBlank() }?.let { "Neighborhood is required." }
        val streetAddressError = state.streetAddress.takeIf { it.isBlank() }?.let { "Street address is required." }
        val countryError = state.country.takeIf { it.isBlank() }?.let { "Country is required." }
        val postalCodeError = state.postalCode.takeIf { it.isBlank() }?.let { "Postal code is required." }
        val shippingMethodOption = shippingOptions.firstOrNull { it.id == state.shippingMethod }
        val paymentOption = paymentOptions.firstOrNull { it.id == state.paymentMethod }
        val validationError = when {
            state.items.isEmpty() -> "Your cart is empty."
            fullNameError != null || phoneNumberError != null || stateError != null || municipalityError != null || neighborhoodError != null || streetAddressError != null || countryError != null || postalCodeError != null ->
                "Complete the shipping information before placing the order."
            shippingMethodOption == null -> "Select a valid shipping method."
            paymentOption == null -> "Select a valid payment method."
            state.paymentMethod == "card" && state.cardNumber.length < 8 -> "Enter a valid card number."
            state.paymentMethod == "card" && state.cardName.isBlank() -> "Enter the cardholder name."
            state.paymentMethod == "card" && state.expiryDate.length < 4 -> "Enter the expiry date."
            state.paymentMethod == "card" && state.cvv.length < 3 -> "Enter the CVV."
            else -> null
        }
        if (validationError != null) {
            formState.update {
                it.copy(
                    fullNameError = fullNameError,
                    phoneNumberError = phoneNumberError,
                    stateError = stateError,
                    municipalityError = municipalityError,
                    neighborhoodError = neighborhoodError,
                    streetAddressError = streetAddressError,
                    countryError = countryError,
                    postalCodeError = postalCodeError,
                    errorMessage = validationError,
                )
            }
            return
        }

        viewModelScope.launch {
            formState.update {
                it.copy(
                    isPlacingOrder = true,
                    errorMessage = null,
                    fullNameError = null,
                    phoneNumberError = null,
                    stateError = null,
                    municipalityError = null,
                    neighborhoodError = null,
                    streetAddressError = null,
                    countryError = null,
                    postalCodeError = null,
                )
            }
            Log.d(
                TAG,
                "Checkout form state before submit: fullName='${state.fullName}', phone='${state.phoneNumber}', state='${state.state}', municipality='${state.municipality}', neighborhood='${state.neighborhood}', streetAddress='${state.streetAddress}', postalCode='${state.postalCode}', country='${state.country}'",
            )
            Log.d(
                TAG,
                "Checkout selection before submit: shippingLabel='${shippingMethodOption?.title}', shippingBackend='${shippingMethodOption?.id}', paymentLabel='${paymentOption?.title}', paymentBackend='${paymentOption?.backendValue}', cartId=${cartRepository.getCheckoutCartId()}",
            )
            runCatching {
                orderRepository.updateAddress(
                    Address(
                        id = "checkout_address",
                        label = "Checkout",
                        fullName = state.fullName,
                        phoneNumber = state.phoneNumber,
                        state = state.state,
                        municipality = state.municipality,
                        neighborhood = state.neighborhood,
                        street = state.streetAddress,
                        postalCode = state.postalCode,
                        country = state.country,
                        isPrimary = true,
                    ),
                )
                orderRepository.updateShippingMethod(state.shippingMethod)
                orderRepository.updatePaymentMethod(state.paymentMethod)
                createOrderUseCase()
            }.onSuccess {
                Log.d(TAG, "Checkout completed successfully.")
                formState.value = CheckoutUiState(
                    shippingMethod = orderRepository.checkoutDraft.value.shippingMethod,
                    paymentMethod = orderRepository.checkoutDraft.value.paymentMethod,
                )
                onPlaced()
            }.onFailure { error ->
                val apiError = error.toApiException()
                Log.d(TAG, "Checkout failed. error=${apiError.message} validation=${apiError.validationErrors}")
                formState.update {
                    it.copy(
                        isPlacingOrder = false,
                        errorMessage = apiError.validationErrors.values
                            .flatten()
                            .joinToString("\n")
                            .ifBlank { apiError.message },
                    )
                }
            }
        }
    }
}
