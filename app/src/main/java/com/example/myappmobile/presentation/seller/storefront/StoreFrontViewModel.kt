package com.example.myappmobile.presentation.seller.storefront

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myappmobile.core.di.AppContainer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class StoreFrontViewModel(
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val sellerId: String = savedStateHandle["sellerId"] ?: "s1"
    private val isAlternateProductLayout = MutableStateFlow(false)

    val uiState: StateFlow<StoreFrontUiState> = combine(
        AppContainer.uiPreferencesRepository.accountProfiles,
        AppContainer.uiPreferencesRepository.storeConfigurations,
        AppContainer.uiPreferencesRepository.sellerApprovalStatuses,
        isAlternateProductLayout,
    ) { _, _, _, alternateLayout -> alternateLayout }
        .mapLatest { alternateLayout ->
            val products = AppContainer.getStoreProductsUseCase(sellerId)
            StoreFrontUiState(
                store = AppContainer.getStoreDetailsUseCase(sellerId),
                products = products,
                reviews = AppContainer.storeRepository.getStoreReviews(sellerId),
                isAlternateProductLayout = alternateLayout,
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = StoreFrontUiState(),
        )

    fun onToggleProductLayout() {
        isAlternateProductLayout.update { !it }
    }
}
