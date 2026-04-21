package com.example.myappmobile.presentation.seller.manageproducts

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myappmobile.core.di.AppContainer
import com.example.myappmobile.data.remote.toApiException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SellerProductDetailViewModel(
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val productId: String = savedStateHandle.get<String>("productId").orEmpty()
    private val _uiState = MutableStateFlow(SellerProductDetailUiState())
    val uiState: StateFlow<SellerProductDetailUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            val user = AppContainer.authRepository.currentUser.value
            Log.d(TAG, "Refreshing seller product details. userId=${user.id} isSeller=${user.isSeller} productId=$productId")
            if (!user.isSeller) {
                _uiState.value = SellerProductDetailUiState(
                    isLoading = false,
                    isSeller = false,
                    errorMessage = "Seller tools are available only for seller accounts.",
                )
                return@launch
            }
            if (productId.isBlank()) {
                _uiState.value = SellerProductDetailUiState(
                    isLoading = false,
                    isSeller = true,
                    errorMessage = "This product could not be opened.",
                )
                return@launch
            }

            _uiState.update { it.copy(isLoading = true, isSeller = true, errorMessage = null) }
            AppContainer.sellerManagementRepository.fetchProductDetailsForSeller(
                sellerId = user.id,
                productId = productId,
            ).fold(
                onSuccess = { details ->
                    _uiState.value = SellerProductDetailUiState(
                        isLoading = false,
                        isSeller = true,
                        details = details,
                        pendingDelete = _uiState.value.pendingDelete,
                        isDeleting = _uiState.value.isDeleting,
                    )
                },
                onFailure = { error ->
                    _uiState.value = SellerProductDetailUiState(
                        isLoading = false,
                        isSeller = true,
                        errorMessage = error.toApiException().message,
                    )
                },
            )
        }
    }

    fun requestDelete() {
        _uiState.update { it.copy(pendingDelete = true, errorMessage = null) }
    }

    fun dismissDelete() {
        _uiState.update { it.copy(pendingDelete = false, isDeleting = false) }
    }

    fun deleteProduct() {
        val user = AppContainer.authRepository.currentUser.value
        if (!user.isSeller) {
            _uiState.update { it.copy(errorMessage = "Seller tools are available only for seller accounts.") }
            return
        }
        if (productId.isBlank()) {
            _uiState.update { it.copy(errorMessage = "This product could not be deleted because the id is missing.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isDeleting = true, errorMessage = null) }
            Log.d(TAG, "Deleting seller product from details. userId=${user.id} isSeller=${user.isSeller} productId=$productId")
            AppContainer.sellerManagementRepository.deleteProduct(user.id, productId).fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            pendingDelete = false,
                            isDeleting = false,
                            deleted = true,
                        )
                    }
                },
                onFailure = { error ->
                    val apiError = error.toApiException()
                    _uiState.update {
                        it.copy(
                            pendingDelete = false,
                            isDeleting = false,
                            errorMessage = apiError.message,
                        )
                    }
                },
            )
        }
    }

    private companion object {
        const val TAG = "SellerProductDetailVM"
    }
}
