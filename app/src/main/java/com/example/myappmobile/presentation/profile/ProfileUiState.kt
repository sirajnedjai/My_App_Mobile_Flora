package com.example.myappmobile.presentation.profile

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.annotation.StringRes
import com.example.myappmobile.domain.model.SellerApprovalStatus
import com.example.myappmobile.domain.model.User

data class ProfileUiState(
    val user: User? = null,
    val phoneNumber: String = "",
    val address: String = "",
    val darkModeEnabled: Boolean = false,
    val buyerSettings: List<ProfileSettingItemUi> = emptyList(),
    val sellerSettings: List<ProfileSettingItemUi> = emptyList(),
    val sellerDashboardSummary: SellerDashboardSummaryUi? = null,
    val sellerDashboardLoading: Boolean = false,
    val sellerDashboardError: String? = null,
    val showSellerTools: Boolean = false,
    val selectedLanguageCode: String = "en",
    val sellerApprovalStatus: SellerApprovalStatus = SellerApprovalStatus.NOT_VERIFIED,
)

data class ProfileSettingItemUi(
    val id: String,
    @StringRes val titleRes: Int,
    @StringRes val subtitleRes: Int,
    val icon: ImageVector,
    val subtitleOverride: String? = null,
)

data class SellerDashboardSummaryUi(
    val storeName: String = "",
    val totalProducts: Int = 0,
    val totalOrders: Int = 0,
    val pendingOrders: Int = 0,
    val deliveredOrders: Int = 0,
    val lowStockProducts: Int = 0,
    val availableBalance: Double = 0.0,
    val lifetimeEarnings: Double = 0.0,
    val insight: String = "",
)
