package com.example.myappmobile.presentation.profile.seller

import com.example.myappmobile.domain.model.SellerApprovalStatus

data class StoreConfigurationUiState(
    val fullName: String = "",
    val phoneNumber: String = "",
    val nationalId: String = "",
    val address: String = "",
    val shopName: String = "",
    val logoUri: String = "",
    val description: String = "",
    val documentUri: String = "",
    val documentLabel: String = "",
    val approvalStatus: SellerApprovalStatus = SellerApprovalStatus.NOT_VERIFIED,
    val rejectionReason: String? = null,
    val submittedAt: String? = null,
    val reviewedAt: String? = null,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val hasUserEdits: Boolean = false,
    val fieldErrors: SellerVerificationFieldErrors = SellerVerificationFieldErrors(),
    val isSeller: Boolean = false,
)

data class SellerVerificationFieldErrors(
    val fullName: String? = null,
    val phoneNumber: String? = null,
    val nationalId: String? = null,
    val address: String? = null,
    val shopName: String? = null,
    val description: String? = null,
    val document: String? = null,
) {
    fun hasErrors(): Boolean = listOf(
        fullName,
        phoneNumber,
        nationalId,
        address,
        shopName,
        description,
        document,
    ).any { !it.isNullOrBlank() }
}
