package com.example.myappmobile.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myappmobile.core.di.AppContainer
import com.example.myappmobile.data.MockData
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {
    private val getProductsUseCase = AppContainer.getProductsUseCase
    private val productRepository = AppContainer.productRepository

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadHomeData()
    }

    private fun loadHomeData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            delay(600)
            _uiState.update {
                it.copy(
                    isLoading = false,
                    banner = MockData.banner,
                    categories = MockData.categories,
                    featuredProducts = getProductsUseCase.featured(),
                    newArrivals = getProductsUseCase.newArrivals(),
                )
            }
        }
    }

    fun onEmailChanged(value: String) {
        _uiState.update { it.copy(emailInput = value) }
    }

    fun onSubscribe() {
        _uiState.update { it.copy(isSubscribed = true) }
    }

    fun onToggleFavorite(productId: String) {
        viewModelScope.launch {
            productRepository.toggleFavorite(productId)
            _uiState.update { state ->
                state.copy(
                    featuredProducts = getProductsUseCase.featured(),
                    newArrivals = getProductsUseCase.newArrivals(),
                )
            }
        }
    }
}
