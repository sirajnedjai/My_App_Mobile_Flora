package com.example.myappmobile.core.di

import com.example.myappmobile.data.repository.AuthRepositoryImpl
import com.example.myappmobile.data.repository.CartRepositoryImpl
import com.example.myappmobile.data.repository.OrderRepositoryImpl
import com.example.myappmobile.data.repository.ProductRepositoryImpl
import com.example.myappmobile.data.repository.StoreRepositoryImpl
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
    val authRepository = AuthRepositoryImpl()
    val productRepository = ProductRepositoryImpl()
    val cartRepository = CartRepositoryImpl()
    val orderRepository = OrderRepositoryImpl(cartRepository)
    val storeRepository = StoreRepositoryImpl()

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
