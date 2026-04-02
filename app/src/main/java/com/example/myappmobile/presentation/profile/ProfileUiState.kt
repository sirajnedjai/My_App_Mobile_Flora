package com.example.myappmobile.presentation.profile

import androidx.compose.ui.graphics.vector.ImageVector
import com.example.myappmobile.domain.model.User

data class ProfileUiState(
    val user: User? = null,
    val darkModeEnabled: Boolean = false,
    val buyerSettings: List<ProfileSettingItemUi> = emptyList(),
    val sellerSettings: List<ProfileSettingItemUi> = emptyList(),
)

data class ProfileSettingItemUi(
    val id: String,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
)
