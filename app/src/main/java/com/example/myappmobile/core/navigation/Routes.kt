package com.example.myappmobile.core.navigation

object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val FORGOT_PASSWORD = "forgot_password"
    const val VERIFICATION = "verification"

    const val HOME = "home"
    const val SEARCH = "search"
    const val SELLER = "seller"
    const val SELLER_PRODUCTS = "seller/products"
    const val SELLER_ABOUT = "seller/about"
    const val SELLER_REVIEWS = "seller/reviews"
    const val WISHLIST = "wishlist"
    const val PROFILE = "profile"

    const val CART = "cart"
    const val CHECKOUT = "checkout/address"
    const val CHECKOUT_SHIPPING = "checkout/shipping"
    const val CHECKOUT_PAYMENT = "checkout/payment"
    const val CHECKOUT_CONFIRMATION = "checkout/confirmation"
    const val PRODUCT_DETAILS = "product_details/{productId}"

    fun productDetails(productId: String): String = "product_details/$productId"
}
