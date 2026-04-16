package com.example.myappmobile.core.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.annotation.StringRes
import com.example.myappmobile.R

data class BottomNavItem(
    val route: String,
    @StringRes val labelRes: Int,
    val icon: ImageVector,
)

val bottomNavItems = listOf(
    BottomNavItem(Routes.HOME, R.string.nav_home, Icons.Outlined.Home),
    BottomNavItem(Routes.SEARCH, R.string.nav_search, Icons.Outlined.Search),
    BottomNavItem(Routes.SELLER, R.string.nav_shop, Icons.Outlined.Storefront),
    BottomNavItem(Routes.WISHLIST, R.string.nav_wishlist, Icons.Outlined.FavoriteBorder),
    BottomNavItem(Routes.PROFILE, R.string.nav_profile, Icons.Outlined.Person),
)
