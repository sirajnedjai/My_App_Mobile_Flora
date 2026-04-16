package com.example.myappmobile.presentation.profile

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.annotation.StringRes
import com.example.myappmobile.domain.model.User

data class ProfileUiState(
    val user: User? = null,
    val phoneNumber: String = "",
    val address: String = "",
    val darkModeEnabled: Boolean = false,
    val buyerSettings: List<ProfileSettingItemUi> = emptyList(),
    val sellerSettings: List<ProfileSettingItemUi> = emptyList(),
    val showSellerTools: Boolean = false,
    val selectedLanguageCode: String = "en",
)

data class ProfileSettingItemUi(
    val id: String,
    @StringRes val titleRes: Int,
    @StringRes val subtitleRes: Int,
    val icon: ImageVector,
)
