package com.example.myappmobile.domain.model

data class User(
    val id: String,
    val fullName: String,
    val email: String,
    val phone: String = "",
    val address: String = "",
    val avatarUrl: String = "",
    val role: String = "",
    val storeName: String = "",
    val verificationStatus: SellerApprovalStatus = SellerApprovalStatus.NOT_VERIFIED,
    val sellerApprovalStatus: SellerApprovalStatus = SellerApprovalStatus.NOT_VERIFIED,
    val membershipTier: String = "PREMIUM MEMBER",
    val isAuthenticated: Boolean = false,
    val isSeller: Boolean = false,
)
