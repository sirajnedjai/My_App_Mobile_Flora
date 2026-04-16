package com.example.myappmobile.core.di

import com.example.myappmobile.data.repository.AuthRepositoryImpl
import com.example.myappmobile.data.repository.AccountSettingsRepository
import com.example.myappmobile.data.repository.AndroidLocalNotificationGateway
import com.example.myappmobile.data.repository.CartRepositoryImpl
import com.example.myappmobile.data.repository.NotificationRepository
import com.example.myappmobile.data.repository.NotificationBackendApi
import com.example.myappmobile.data.repository.NotificationNavigationRepository
import com.example.myappmobile.data.repository.OrderRepositoryImpl
import com.example.myappmobile.data.repository.OrderNotificationService
import com.example.myappmobile.data.repository.ProductRepositoryImpl
import com.example.myappmobile.data.repository.ProductReviewRepository
import com.example.myappmobile.data.repository.ReviewEligibilityService
import com.example.myappmobile.data.repository.SearchHistoryRepository
import com.example.myappmobile.data.repository.SellerManagementRepository
import com.example.myappmobile.data.repository.ShopFilterRepository
import com.example.myappmobile.data.repository.StoreRepositoryImpl
import com.example.myappmobile.data.repository.UiPreferencesRepository
import com.example.myappmobile.domain.usecase.auth.LoginUseCase
import com.example.myappmobile.domain.usecase.auth.RegisterUseCase
import com.example.myappmobile.domain.usecase.cart.AddToCartUseCase
import com.example.myappmobile.domain.usecase.cart.GetCartItemsUseCase
import com.example.myappmobile.domain.usecase.cart.RemoveFromCartUseCase
import com.example.myappmobile.domain.usecase.order.CreateOrderUseCase
import com.example.myappmobile.domain.usecase.order.GetOrdersUseCase
import com.example.myappmobile.domain.usecase.product.GetProductDetailsUseCase
import com.example.myappmobile.domain.usecase.product.GetProductsUseCase
import com.example.myappmobile.domain.usecase.product.SearchProductsUseCase
import com.example.myappmobile.domain.usecase.store.GetStoreDetailsUseCase
import com.example.myappmobile.domain.usecase.store.GetStoreProductsUseCase

object AppContainer {
    val uiPreferencesRepository = UiPreferencesRepository()
    val accountSettingsRepository = AccountSettingsRepository()
    val notificationRepository = NotificationRepository()
    val notificationBackendApi = NotificationBackendApi()
    val notificationNavigationRepository = NotificationNavigationRepository()
    val localNotificationGateway = AndroidLocalNotificationGateway()
    val searchHistoryRepository = SearchHistoryRepository()
    val shopFilterRepository = ShopFilterRepository()
    val authRepository = AuthRepositoryImpl()
    val productRepository = ProductRepositoryImpl()
    val cartRepository = CartRepositoryImpl()
    val orderRepository = OrderRepositoryImpl(cartRepository, authRepository)
    val notificationService = OrderNotificationService(
        notificationBackendApi = notificationBackendApi,
        accountSettingsRepository = accountSettingsRepository,
    )
    val reviewEligibilityService = ReviewEligibilityService()
    val productReviewRepository = ProductReviewRepository(authRepository, orderRepository, reviewEligibilityService)
    val storeRepository = StoreRepositoryImpl()
    val sellerManagementRepository = SellerManagementRepository()

    val loginUseCase = LoginUseCase(authRepository)
    val registerUseCase = RegisterUseCase(authRepository)
    val getProductsUseCase = GetProductsUseCase(productRepository)
    val getProductDetailsUseCase = GetProductDetailsUseCase(productRepository)
    val searchProductsUseCase = SearchProductsUseCase(productRepository)
    val getCartItemsUseCase = GetCartItemsUseCase(cartRepository)
    val addToCartUseCase = AddToCartUseCase(cartRepository)
    val removeFromCartUseCase = RemoveFromCartUseCase(cartRepository)
    val createOrderUseCase = CreateOrderUseCase(orderRepository)
    val getOrdersUseCase = GetOrdersUseCase(orderRepository)
    val getStoreDetailsUseCase = GetStoreDetailsUseCase(storeRepository)
    val getStoreProductsUseCase = GetStoreProductsUseCase(storeRepository)
}
