package com.example.myappmobile.presentation.checkout.address

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myappmobile.core.di.AppContainer
import com.example.myappmobile.domain.model.Address
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AddressViewModel : ViewModel() {

    private val repository = AppContainer.orderRepository
    private val _uiState = MutableStateFlow(AddressUiState())
    val uiState: StateFlow<AddressUiState> = _uiState.asStateFlow()

    init {
        repository.checkoutDraft.value.address?.let { address ->
            _uiState.value = AddressUiState(
                fullName = address.fullName,
                state = address.state,
                municipality = address.municipality,
                neighborhood = address.neighborhood,
                streetAddress = address.street,
                postalCode = address.postalCode,
                country = address.country,
            )
        }
    }

    fun onFullNameChange(value: String) = _uiState.update { it.copy(fullName = value) }
    fun onStateChange(value: String) = _uiState.update { it.copy(state = value) }
    fun onMunicipalityChange(value: String) = _uiState.update { it.copy(municipality = value) }
    fun onNeighborhoodChange(value: String) = _uiState.update { it.copy(neighborhood = value) }
    fun onStreetChange(value: String) = _uiState.update { it.copy(streetAddress = value) }
    fun onPostalCodeChange(value: String) = _uiState.update { it.copy(postalCode = value) }
    fun onCountryChange(value: String) = _uiState.update { it.copy(country = value) }

    fun saveAddress(onSaved: () -> Unit) {
        viewModelScope.launch {
            val state = _uiState.value
            repository.updateAddress(
                Address(
                    id = "checkout_address",
                    label = "Checkout",
                    fullName = state.fullName,
                    state = state.state,
                    municipality = state.municipality,
                    neighborhood = state.neighborhood,
                    street = state.streetAddress,
                    postalCode = state.postalCode,
                    country = state.country,
                    isPrimary = true,
                )
            )
            onSaved()
        }
    }
}
