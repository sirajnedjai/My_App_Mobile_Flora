package com.example.myappmobile.domain.model

data class User(
    val id: String,
    val fullName: String,
    val email: String,
    val phone: String = "",
    val avatarUrl: String = "",
    val membershipTier: String = "PREMIUM MEMBER",
    val isAuthenticated: Boolean = false,
    val isSeller: Boolean = false,
)
