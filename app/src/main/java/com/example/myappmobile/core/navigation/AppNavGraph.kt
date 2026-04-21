package com.example.myappmobile.core.navigation

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.myappmobile.core.di.AppContainer
import com.example.myappmobile.presentation.auth.forgotpassword.ForgotPasswordScreen
import com.example.myappmobile.presentation.auth.login.LoginScreen
import com.example.myappmobile.presentation.auth.register.RegisterScreen
import com.example.myappmobile.presentation.auth.verification.VerificationScreen
import com.example.myappmobile.presentation.cart.CartScreen
import com.example.myappmobile.presentation.checkout.CheckoutScreen
import com.example.myappmobile.presentation.checkout.confirmation.ConfirmationScreen
import com.example.myappmobile.presentation.checkout.payment.PaymentScreen
import com.example.myappmobile.presentation.checkout.shipping.ShippingScreen
import com.example.myappmobile.presentation.home.HomeScreen
import com.example.myappmobile.presentation.home.HomeViewModel
import com.example.myappmobile.presentation.orders.tracking.OrderTrackingScreen
import com.example.myappmobile.presentation.orders.tracking.ShipmentTrackingScreen
import com.example.myappmobile.presentation.orders.tracking.ShipmentTrackingViewModel
import com.example.myappmobile.presentation.orders.detail.OrderDetailScreen
import com.example.myappmobile.presentation.orders.detail.OrderDetailViewModel
import com.example.myappmobile.presentation.productdetails.ProductDetailsScreen
import com.example.myappmobile.presentation.productdetails.ProductDetailsViewModel
import com.example.myappmobile.presentation.profile.ProfileScreen
import com.example.myappmobile.presentation.profile.account.PersonalInformationScreen
import com.example.myappmobile.presentation.profile.settings.NotificationsScreen
import com.example.myappmobile.presentation.profile.settings.PasswordSecurityScreen
import com.example.myappmobile.presentation.profile.settings.PaymentMethodsScreen
import com.example.myappmobile.presentation.profile.settings.SavedAddressesScreen
import com.example.myappmobile.presentation.profile.seller.PaymentsPayoutsScreen
import com.example.myappmobile.presentation.profile.seller.SellerNotificationsScreen
import com.example.myappmobile.presentation.profile.seller.SellerShippingLogisticsScreen
import com.example.myappmobile.presentation.profile.seller.SellerWithdrawalScreen
import com.example.myappmobile.presentation.profile.seller.StoreConfigurationScreen
import com.example.myappmobile.presentation.search.SearchScreen
import com.example.myappmobile.presentation.seller.manageproducts.SellerProductManagementScreen
import com.example.myappmobile.presentation.seller.manageproducts.SellerProductDetailScreen
import com.example.myappmobile.presentation.seller.manageproducts.SellerProductDetailViewModel
import com.example.myappmobile.presentation.shop.ShopScreen
import com.example.myappmobile.presentation.shop.ShopFilterScreen
import com.example.myappmobile.presentation.seller.about.AboutStoreScreen
import com.example.myappmobile.presentation.seller.binary.SellerBinaryProductsScreen
import com.example.myappmobile.presentation.seller.orders.SellerOrdersScreen
import com.example.myappmobile.presentation.seller.orders.SellerOrderDetailScreen
import com.example.myappmobile.presentation.seller.products.StoreProductsScreen
import com.example.myappmobile.presentation.seller.reviews.StoreReviewsScreen
import com.example.myappmobile.presentation.seller.storefront.StoreFrontScreen
import com.example.myappmobile.presentation.testing.product.ProductTestScreen
import com.example.myappmobile.presentation.testing.user.UserTestScreen
import com.example.myappmobile.presentation.wishlist.WishlistScreen

private const val APP_NAV_GRAPH_TAG = "LOGIN_DEBUG"

@Composable
fun AppNavGraph(
    navController: NavHostController = rememberNavController(),
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val currentUser by AppContainer.authRepository.currentUser.collectAsState()
    val pendingNotificationTarget by AppContainer.notificationNavigationRepository.pendingTarget.collectAsState()
    val authRoutes = remember {
        setOf(
            Routes.LOGIN,
            Routes.REGISTER,
            Routes.FORGOT_PASSWORD,
            Routes.VERIFICATION,
        )
    }
    val landingRoute = remember(currentUser.isAuthenticated, currentUser.isSeller) {
        resolveLandingRoute(currentUser)
    }
    LaunchedEffect(currentRoute, landingRoute, currentUser.isAuthenticated, currentUser.isSeller) {
        Log.d(
            APP_NAV_GRAPH_TAG,
            "Nav graph active. currentRoute=${currentRoute.orEmpty()} landingRoute=$landingRoute authenticated=${currentUser.isAuthenticated} seller=${currentUser.isSeller}",
        )
    }
    val navigateToTopLevel: (String) -> Unit = { route ->
        if (route != currentRoute) {
            navController.navigate(route) {
                popUpTo(Routes.HOME) { saveState = true }
                launchSingleTop = true
                restoreState = true
            }
        }
    }

    LaunchedEffect(currentUser.isAuthenticated, currentUser.isSeller, currentRoute) {
        when {
            currentUser.isAuthenticated && currentRoute in authRoutes && currentRoute != landingRoute -> {
                Log.d(APP_NAV_GRAPH_TAG, "Authenticated user redirected to $landingRoute")
                navController.navigate(landingRoute) {
                    popUpTo(0) { inclusive = true }
                    launchSingleTop = true
                }
            }
            !currentUser.isAuthenticated && currentRoute != null && currentRoute !in authRoutes -> {
                Log.d(APP_NAV_GRAPH_TAG, "Unauthenticated user redirected to ${Routes.LOGIN}")
                navController.navigate(Routes.LOGIN) {
                    popUpTo(0) { inclusive = true }
                    launchSingleTop = true
                }
            }
        }
    }

    LaunchedEffect(currentUser.isAuthenticated, pendingNotificationTarget?.orderId) {
        val target = pendingNotificationTarget ?: return@LaunchedEffect
        if (!currentUser.isAuthenticated) return@LaunchedEffect
        if (target.destination == "order_tracking") {
            navController.navigate(Routes.ORDER_TRACKING) {
                launchSingleTop = true
            }
            AppContainer.notificationNavigationRepository.consume()
        }
    }

    NavHost(
        navController = navController,
        startDestination = if (currentUser.isAuthenticated) landingRoute else Routes.LOGIN,
    ) {
        composable(Routes.LOGIN) {
            LoginScreen(
                onNavigateToRegister = { navController.navigate(Routes.REGISTER) },
                onNavigateToForgotPassword = { navController.navigate(Routes.FORGOT_PASSWORD) },
            )
        }

        composable(Routes.REGISTER) {
            RegisterScreen(
                onNavigateToLogin = { navController.popBackStack() },
                onRegisterSuccess = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.REGISTER) { inclusive = true }
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
                onCategorySelection = { categoryId, subcategoryId ->
                    AppContainer.shopFilterRepository.applyCategoryShortcut(categoryId, subcategoryId)
                    navController.navigate(Routes.SELLER) {
                        launchSingleTop = true
                    }
                },
                onProductClick = { productId ->
                    navController.navigate(Routes.productDetails(productId))
                },
                onEmailChange = viewModel::onEmailChanged,
                onSubscribe = viewModel::onSubscribe,
                onFavoriteToggle = viewModel::onToggleFavorite,
                onFavoriteMessageShown = viewModel::clearFavoriteMessage,
                onRetry = viewModel::retry,
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
            val viewModel: com.example.myappmobile.presentation.shop.ShopViewModel = viewModel()
            ShopScreen(
                onProductClick = { productId ->
                    navController.navigate(Routes.productDetails(productId))
                },
                onBannerClick = { navController.navigate(Routes.sellerStorefront("s1")) },
                onCartClick = { navController.navigate(Routes.CART) },
                onFilterClick = { navController.navigate(Routes.SHOP_FILTERS) },
                onRetry = viewModel::retry,
                selectedRoute = Routes.SELLER,
                onBottomNavClick = navigateToTopLevel,
                viewModel = viewModel,
            )
        }

        composable(Routes.SHOP_FILTERS) {
            ShopFilterScreen(
                onBack = { navController.popBackStack() },
                onApplied = { navController.popBackStack() },
            )
        }

        composable(
            route = Routes.SELLER_STOREFRONT,
            arguments = listOf(navArgument("sellerId") { type = NavType.StringType }),
        ) {
            StoreFrontScreen(
                onBack = { navController.popBackStack() },
                onOpenAbout = {
                    val sellerId = it.arguments?.getString("sellerId").orEmpty().ifBlank { "s1" }
                    navController.navigate(Routes.sellerAbout(sellerId))
                },
                onOpenReviews = {
                    val sellerId = it.arguments?.getString("sellerId").orEmpty().ifBlank { "s1" }
                    navController.navigate(Routes.sellerReviews(sellerId))
                },
                onProductClick = { productId ->
                    navController.navigate(Routes.productDetails(productId))
                },
                selectedRoute = Routes.SELLER,
                onBottomNavClick = navigateToTopLevel,
            )
        }

        composable(
            route = Routes.SELLER_PRODUCTS,
            arguments = listOf(navArgument("sellerId") { type = NavType.StringType }),
        ) {
            StoreProductsScreen()
        }

        composable(
            route = Routes.SELLER_BINARY_PRODUCTS,
            arguments = listOf(navArgument("sellerId") { type = NavType.StringType }),
        ) {
            SellerBinaryProductsScreen(
                onBack = { navController.popBackStack() },
            )
        }

        composable(
            route = Routes.SELLER_ABOUT,
            arguments = listOf(navArgument("sellerId") { type = NavType.StringType }),
        ) {
            AboutStoreScreen()
        }

        composable(
            route = Routes.SELLER_REVIEWS,
            arguments = listOf(navArgument("sellerId") { type = NavType.StringType }),
        ) {
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
                        "personal_information" -> navController.navigate(Routes.PERSONAL_INFORMATION)
                        "password_security" -> navController.navigate(Routes.PASSWORD_SECURITY)
                        "payment_methods" -> navController.navigate(Routes.PAYMENT_METHODS)
                        "buyer_notifications" -> navController.navigate(Routes.NOTIFICATIONS)
                        "track_my_orders" -> navController.navigate(Routes.ORDER_TRACKING)
                        "saved_addresses" -> navController.navigate(Routes.SAVED_ADDRESSES)
                        "store_configuration" -> navController.navigate(Routes.STORE_CONFIGURATION)
                        "payments_payouts" -> navController.navigate(Routes.PAYMENTS_PAYOUTS)
                        "shipping_logistics" -> navController.navigate(Routes.SELLER_SHIPPING_LOGISTICS)
                        "seller_notifications" -> navController.navigate(Routes.SELLER_NOTIFICATIONS)
                        "manage_products" -> navController.navigate(Routes.SELLER_MANAGE_PRODUCTS)
                        "received_orders" -> navController.navigate(Routes.SELLER_RECEIVED_ORDERS)
                    }
                },
                onLogoutClick = {
                    AppContainer.authRepository.logout()
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
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
                onContinueShopping = { navController.navigate(Routes.SELLER) },
            )
        }

        composable(Routes.ORDER_TRACKING) {
            OrderTrackingScreen(
                onBack = { navController.popBackStack() },
                onContinueShopping = { navController.navigate(Routes.SELLER) },
                onOpenOrder = { orderId -> navController.navigate(Routes.orderDetail(orderId)) },
            )
        }

        composable(
            route = Routes.ORDER_TRACKING_DETAIL,
            arguments = listOf(navArgument("orderId") { type = NavType.StringType }),
        ) {
            val viewModel: ShipmentTrackingViewModel = viewModel()
            ShipmentTrackingScreen(
                onBack = { navController.popBackStack() },
                onRetry = viewModel::retry,
                viewModel = viewModel,
            )
        }

        composable(
            route = Routes.ORDER_DETAIL,
            arguments = listOf(navArgument("orderId") { type = NavType.StringType }),
        ) {
            val viewModel: OrderDetailViewModel = viewModel()
            OrderDetailScreen(
                onBack = { navController.popBackStack() },
                onRetry = viewModel::retry,
                onTrackShipment = { orderId -> navController.navigate(Routes.orderTrackingDetail(orderId)) },
                viewModel = viewModel,
            )
        }

        composable(Routes.PERSONAL_INFORMATION) {
            PersonalInformationScreen(
                onBack = { navController.popBackStack() },
            )
        }

        composable(Routes.PASSWORD_SECURITY) {
            PasswordSecurityScreen(
                onBack = { navController.popBackStack() },
            )
        }

        composable(Routes.SAVED_ADDRESSES) {
            SavedAddressesScreen(
                onBack = { navController.popBackStack() },
            )
        }

        composable(Routes.PAYMENT_METHODS) {
            PaymentMethodsScreen(
                onBack = { navController.popBackStack() },
            )
        }

        composable(Routes.NOTIFICATIONS) {
            NotificationsScreen(
                onBack = { navController.popBackStack() },
                onOpenOrder = { navController.navigate(Routes.ORDER_TRACKING) },
            )
        }

        composable(Routes.STORE_CONFIGURATION) {
            StoreConfigurationScreen(
                onBack = { navController.popBackStack() },
            )
        }

        composable(Routes.PAYMENTS_PAYOUTS) {
            PaymentsPayoutsScreen(
                onBack = { navController.popBackStack() },
                onWithdrawClick = { navController.navigate(Routes.SELLER_WITHDRAWAL) },
            )
        }

        composable(Routes.SELLER_WITHDRAWAL) {
            SellerWithdrawalScreen(
                onBack = { navController.popBackStack() },
            )
        }

        composable(Routes.SELLER_SHIPPING_LOGISTICS) {
            SellerShippingLogisticsScreen(
                onBack = { navController.popBackStack() },
            )
        }

        composable(Routes.SELLER_NOTIFICATIONS) {
            SellerNotificationsScreen(
                onBack = { navController.popBackStack() },
                onOpenOrder = { orderId ->
                    navController.navigate(Routes.sellerOrderDetail(orderId))
                },
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
            CheckoutScreen(
                onBack = { navController.popBackStack() },
                onPlaceOrderSuccess = {
                    navController.navigate(Routes.CHECKOUT_CONFIRMATION) {
                        launchSingleTop = true
                    }
                },
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

        composable(Routes.ROOM_USERS_TEST) {
            UserTestScreen(
                onBack = { navController.popBackStack() },
            )
        }

        composable(Routes.ROOM_PRODUCTS_TEST) {
            ProductTestScreen(
                onBack = { navController.popBackStack() },
            )
        }

        composable(Routes.SELLER_MANAGE_PRODUCTS) {
            SellerProductManagementScreen(
                onBack = { navController.popBackStack() },
                onOpenProduct = { productId -> navController.navigate(Routes.sellerProductDetail(productId)) },
            )
        }

        composable(
            route = Routes.SELLER_PRODUCT_DETAIL,
            arguments = listOf(navArgument("productId") { type = NavType.StringType }),
        ) {
            val viewModel: SellerProductDetailViewModel = viewModel()
            SellerProductDetailScreen(
                onBack = { navController.popBackStack() },
                onDeleted = { navController.popBackStack() },
                viewModel = viewModel,
            )
        }

        composable(Routes.SELLER_RECEIVED_ORDERS) {
            SellerOrdersScreen(
                onBack = { navController.popBackStack() },
                onOpenOrder = { orderId -> navController.navigate(Routes.sellerOrderDetail(orderId)) },
            )
        }

        composable(
            route = Routes.SELLER_ORDER_DETAIL,
            arguments = listOf(navArgument("orderId") { type = NavType.StringType }),
        ) {
            SellerOrderDetailScreen(
                onBack = { navController.popBackStack() },
            )
        }

        composable(
            route = Routes.PRODUCT_DETAILS,
            arguments = listOf(
                navArgument("productId") { type = NavType.StringType },
                navArgument("orderId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
            ),
        ) {
            val viewModel: ProductDetailsViewModel = viewModel()
            val uiState by viewModel.uiState.collectAsState()
            ProductDetailsScreen(
                uiState = uiState,
                onBack = { navController.popBackStack() },
                onAddToCart = viewModel::onAddToCart,
                onReservePickup = viewModel::onReservePickup,
                onFavoriteToggle = viewModel::onToggleFavorite,
                onFavoriteMessageShown = viewModel::clearFavoriteMessage,
                onVisitStudio = {
                    uiState.sellerId
                        ?.takeIf { it.isNotBlank() }
                        ?.let { sellerId -> navController.navigate(Routes.sellerStorefront(sellerId)) }
                },
                onCartClick = { navController.navigate(Routes.CART) },
                onSelectImage = viewModel::onSelectImage,
                onRatingSelected = viewModel::onRatingSelected,
                onReviewInputChanged = viewModel::onReviewInputChanged,
                onSubmitReview = viewModel::submitReview,
                onRetry = viewModel::retry,
                onSimilarProductClick = { productId ->
                    navController.navigate(Routes.productDetails(productId))
                },
            )
        }
    }
}

private fun resolveLandingRoute(user: com.example.myappmobile.domain.model.User): String =
    if (user.isSeller) {
        Routes.HOME
    } else {
        Routes.SELLER
    }
