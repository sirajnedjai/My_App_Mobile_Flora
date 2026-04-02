package com.example.myappmobile.core.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.ui.graphics.vector.ImageVector

data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector,
)

val bottomNavItems = listOf(
    BottomNavItem(Routes.HOME, "HOME", Icons.Outlined.Home),
    BottomNavItem(Routes.SEARCH, "SEARCH", Icons.Outlined.Search),
    BottomNavItem(Routes.SELLER, "SHOP", Icons.Outlined.Storefront),
    BottomNavItem(Routes.WISHLIST, "WISHLIST", Icons.Outlined.FavoriteBorder),
    BottomNavItem(Routes.PROFILE, "PROFILE", Icons.Outlined.Person),
)
