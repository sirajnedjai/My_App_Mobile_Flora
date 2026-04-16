package com.example.myappmobile.domain.model

data class Store(
    val id: String,
    val name: String,
    val ownerName: String = "",
    val description: String = "",
    val logoUrl: String = "",
    val bannerUrl: String = "",
    val location: String = "",
    val contactEmail: String = "",
    val rating: Float = 0f,
    val reviewCount: Int = 0,
    val practisingSince: String = "",
    val totalSales: Int = 0,
    val activeProducts: Int = 0,
    val monthlyEarnings: Double = 0.0,
    val availableBalance: Double = 0.0,
    val lifetimeEarnings: Double = 0.0,
    val categories: List<String> = emptyList(),
    val story: String = "",
    val approvalStatus: SellerApprovalStatus = SellerApprovalStatus.PENDING,
)

data class Review(
    val id: String,
    val authorName: String,
    val rating: Int,
    val text: String,
    val isVerified: Boolean = true,
    val label: String = "VERIFIED COLLECTOR",
)

data class Collection(
    val id: String,
    val name: String,
    val itemCount: Int,
    val createdDate: String,
    val imageUrl: String = "",
)

data class LedgerEntry(
    val id: String,
    val title: String,
    val date: String,
    val type: String,
    val amount: Double,
    val isPositive: Boolean,
    val status: String,
    val reference: String = "",
)
