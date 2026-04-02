package com.example.myappmobile.core.navigation

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.myappmobile.presentation.auth.forgotpassword.ForgotPasswordScreen
import com.example.myappmobile.presentation.auth.login.LoginScreen
import com.example.myappmobile.presentation.auth.register.RegisterScreen
import com.example.myappmobile.presentation.auth.verification.VerificationScreen
import com.example.myappmobile.presentation.cart.CartScreen
import com.example.myappmobile.presentation.checkout.address.AddressScreen
import com.example.myappmobile.presentation.checkout.confirmation.ConfirmationScreen
import com.example.myappmobile.presentation.checkout.payment.PaymentScreen
import com.example.myappmobile.presentation.checkout.shipping.ShippingScreen
import com.example.myappmobile.presentation.home.HomeScreen
import com.example.myappmobile.presentation.home.HomeViewModel
import com.example.myappmobile.presentation.productdetails.ProductDetailsScreen
import com.example.myappmobile.presentation.productdetails.ProductDetailsViewModel
import com.example.myappmobile.presentation.profile.ProfileScreen
import com.example.myappmobile.presentation.search.SearchScreen
import com.example.myappmobile.presentation.shop.ShopScreen
import com.example.myappmobile.presentation.seller.about.AboutStoreScreen
import com.example.myappmobile.presentation.seller.products.StoreProductsScreen
import com.example.myappmobile.presentation.seller.reviews.StoreReviewsScreen
import com.example.myappmobile.presentation.wishlist.WishlistScreen

private const val APP_NAV_GRAPH_TAG = "LOGIN_DEBUG"

@Composable
fun AppNavGraph(
    navController: NavHostController = rememberNavController(),
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val navigateToTopLevel: (String) -> Unit = { route ->
        if (route != currentRoute) {
            navController.navigate(route) {
                popUpTo(Routes.HOME) { saveState = true }
                launchSingleTop = true
                restoreState = true
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = Routes.LOGIN,
    ) {
        composable(Routes.LOGIN) {
            LoginScreen(
                onNavigateToRegister = { navController.navigate(Routes.REGISTER) },
                onNavigateToForgotPassword = { navController.navigate(Routes.FORGOT_PASSWORD) },
                onLoginSuccess = {
                    val currentRoute = navController.currentDestination?.route
                    Log.d(APP_NAV_GRAPH_TAG, "Login success navigation requested from $currentRoute")
                    if (currentRoute != Routes.HOME) {
                        navController.navigate(Routes.HOME) {
                            popUpTo(Routes.LOGIN) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                },
            )
        }

        composable(Routes.REGISTER) {
            RegisterScreen(
                onNavigateToLogin = { navController.popBackStack() },
                onRegisterSuccess = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                        launchSingleTop = true
                    }
                },
            )
        }

        composable(Routes.FORGOT_PASSWORD) {
            ForgotPasswordScreen(
                onBack = { navController.popBackStack() },
                onContinue = { navController.navigate(Routes.VERIFICATION) },
            )
        }

        composable(Routes.HOME) {
            val viewModel: HomeViewModel = viewModel()
            val uiState by viewModel.uiState.collectAsState()
            HomeScreen(
                uiState = uiState,
                selectedRoute = currentRoute ?: Routes.HOME,
                onProductClick = { productId ->
                    navController.navigate(Routes.productDetails(productId))
                },
                onEmailChange = viewModel::onEmailChanged,
                onSubscribe = viewModel::onSubscribe,
                onFavoriteToggle = viewModel::onToggleFavorite,
                onBottomNavClick = navigateToTopLevel,
                onCartClick = { navController.navigate(Routes.CART) },
            )
        }

        composable(Routes.SEARCH) {
            SearchScreen(
                onBack = { navController.popBackStack() },
                onProductClick = { productId -> navController.navigate(Routes.productDetails(productId)) },
                selectedRoute = Routes.SEARCH,
                onBottomNavClick = navigateToTopLevel,
            )
        }

        composable(Routes.SELLER) {
            ShopScreen(
                onProductClick = { productId ->
                    navController.navigate(Routes.productDetails(productId))
                },
                onBannerClick = { navController.navigate(Routes.SELLER_PRODUCTS) },
                onCartClick = { navController.navigate(Routes.CART) },
                selectedRoute = Routes.SELLER,
                onBottomNavClick = navigateToTopLevel,
            )
        }

        composable(Routes.SELLER_PRODUCTS) {
            StoreProductsScreen()
        }

        composable(Routes.SELLER_ABOUT) {
            AboutStoreScreen()
        }

        composable(Routes.SELLER_REVIEWS) {
            StoreReviewsScreen()
        }

        composable(Routes.WISHLIST) {
            WishlistScreen(
                onBack = { navController.popBackStack() },
                onProductClick = { productId ->
                    navController.navigate(Routes.productDetails(productId))
                },
                onContinueShopping = { navController.navigate(Routes.SELLER) },
                onCartClick = { navController.navigate(Routes.CART) },
                selectedRoute = Routes.WISHLIST,
                onBottomNavClick = navigateToTopLevel,
            )
        }

        composable(Routes.PROFILE) {
            ProfileScreen(
                onBack = { navController.popBackStack() },
                onSettingClick = { settingId ->
                    when (settingId) {
                        "saved_addresses" -> navController.navigate(Routes.CHECKOUT)
                        "store_configuration" -> navController.navigate(Routes.SELLER_ABOUT)
                        "payments_payouts" -> navController.navigate(Routes.SELLER_PRODUCTS)
                        "shipping_logistics" -> navController.navigate(Routes.CHECKOUT_SHIPPING)
                        "seller_notifications" -> navController.navigate(Routes.SELLER_REVIEWS)
                    }
                },
                selectedRoute = Routes.PROFILE,
                onBottomNavClick = navigateToTopLevel,
            )
        }

        composable(Routes.CART) {
            CartScreen(
                onBack = { navController.popBackStack() },
                onCheckout = { navController.navigate(Routes.CHECKOUT) },
            )
        }

        composable(Routes.VERIFICATION) {
            VerificationScreen(
                onVerificationSuccess = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.LOGIN) { inclusive = false }
                        launchSingleTop = true
                    }
                },
            )
        }

        composable(Routes.CHECKOUT) {
            AddressScreen(
                onContinue = { navController.navigate(Routes.CHECKOUT_SHIPPING) },
            )
        }

        composable(Routes.CHECKOUT_SHIPPING) {
            ShippingScreen(
                onContinue = { navController.navigate(Routes.CHECKOUT_PAYMENT) },
            )
        }

        composable(Routes.CHECKOUT_PAYMENT) {
            PaymentScreen(
                onPlaceOrder = {
                    navController.navigate(Routes.CHECKOUT_CONFIRMATION) {
                        launchSingleTop = true
                    }
                },
            )
        }

        composable(Routes.CHECKOUT_CONFIRMATION) {
            ConfirmationScreen(
                onReturnHome = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.HOME) { inclusive = false }
                        launchSingleTop = true
                    }
                },
            )
        }

        composable(
            route = Routes.PRODUCT_DETAILS,
            arguments = listOf(navArgument("productId") { type = NavType.StringType }),
        ) {
            val viewModel: ProductDetailsViewModel = viewModel()
            val uiState by viewModel.uiState.collectAsState()
            ProductDetailsScreen(
                uiState = uiState,
                onBack = { navController.popBackStack() },
                onAddToCart = viewModel::onAddToCart,
                onReservePickup = viewModel::onReservePickup,
                onCartClick = { navController.navigate(Routes.CART) },
                onSelectImage = viewModel::onSelectImage,
                onSimilarProductClick = { productId ->
                    navController.navigate(Routes.productDetails(productId))
                },
            )
        }
    }
}
