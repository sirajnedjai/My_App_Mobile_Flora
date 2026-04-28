package com.example.myappmobile.presentation.profile.account

data class PersonalInformationUiState(
    val fullName: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val address: String = "",
    val storeName: String = "",
    val roleLabel: String = "",
    val membershipTier: String = "",
    val avatarUrl: String = "",
    val isSeller: Boolean = false,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val hasUserEdits: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
)
