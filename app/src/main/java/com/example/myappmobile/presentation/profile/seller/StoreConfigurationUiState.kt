package com.example.myappmobile.presentation.profile.seller

import com.example.myappmobile.domain.model.SellerApprovalStatus

data class StoreConfigurationUiState(
    val shopName: String = "",
    val establishmentDate: String = "",
    val ownerName: String = "",
    val logoUri: String = "",
    val description: String = "",
    val approvalStatus: SellerApprovalStatus = SellerApprovalStatus.PENDING,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val hasUserEdits: Boolean = false,
)
