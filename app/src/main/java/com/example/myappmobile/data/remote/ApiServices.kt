package com.example.myappmobile.data.remote

import com.google.gson.JsonElement
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.PartMap
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface AuthApiService {
    @POST("auth/login")
    suspend fun login(
        @Body body: LoginRequestDto,
    ): Response<LaravelApiResponse<JsonElement>>

    @POST("auth/register")
    suspend fun register(
        @Body body: RegisterRequestDto,
    ): Response<LaravelApiResponse<JsonElement>>

    @POST("auth/logout")
    suspend fun logout(): Response<LaravelApiResponse<JsonElement>>

    @GET("auth/me")
    suspend fun me(): Response<LaravelApiResponse<JsonElement>>

    @POST("account/profile")
    suspend fun updateProfile(
        @Body body: UpdateProfileRequestDto,
    ): Response<LaravelApiResponse<JsonElement>>

    @Multipart
    @POST("account/profile")
    suspend fun updateProfileMultipart(
        @PartMap body: Map<String, @JvmSuppressWildcards RequestBody>,
        @Part avatar: MultipartBody.Part? = null,
    ): Response<LaravelApiResponse<JsonElement>>

    @PUT("account/password")
    suspend fun changePassword(
        @Body body: ChangePasswordRequestDto,
    ): Response<LaravelApiResponse<JsonElement>>
}

interface ProductApiService {
    @GET("products")
    suspend fun getProducts(): Response<LaravelApiResponse<JsonElement>>

    @GET("products/{id}")
    suspend fun getProductDetails(
        @Path("id") productId: String,
    ): Response<LaravelApiResponse<JsonElement>>

    @GET("products/search")
    suspend fun searchProducts(
        @Query("keyword") keyword: String,
    ): Response<LaravelApiResponse<JsonElement>>

    @GET("products/filter")
    suspend fun filterProducts(
        @Query("category") category: String? = null,
        @Query("seller_id") sellerId: String? = null,
        @Query("status") status: String? = null,
        @Query("min_price") minPrice: Double? = null,
        @Query("max_price") maxPrice: Double? = null,
    ): Response<LaravelApiResponse<JsonElement>>

    @GET("products/{id}/reviews")
    suspend fun getProductReviews(
        @Path("id") productId: String,
    ): Response<LaravelApiResponse<JsonElement>>
}

interface StoreApiService {
    @GET("stores/{id}")
    suspend fun getStore(
        @Path("id") storeId: String,
    ): Response<LaravelApiResponse<JsonElement>>

    @GET("stores/seller/{sellerId}")
    suspend fun getStoreBySeller(
        @Path("sellerId") sellerId: String,
    ): Response<LaravelApiResponse<JsonElement>>
}

interface CartApiService {
    @GET("cart")
    suspend fun getCart(): Response<LaravelApiResponse<JsonElement>>

    @POST("cart/add")
    suspend fun addToCart(
        @Body body: AddToCartRequestDto,
    ): Response<LaravelApiResponse<JsonElement>>

    @PUT("cart/item/{id}")
    suspend fun updateCartItem(
        @Path("id") itemId: String,
        @Body body: UpdateCartItemRequestDto,
    ): Response<LaravelApiResponse<JsonElement>>

    @DELETE("cart/item/{id}")
    suspend fun deleteCartItem(
        @Path("id") itemId: String,
    ): Response<LaravelApiResponse<JsonElement>>
}

interface FavoriteApiService {
    @GET("favorites")
    suspend fun getFavorites(): Response<LaravelApiResponse<JsonElement>>

    @POST("favorites")
    suspend fun addFavorite(
        @Body body: FavoriteRequestDto,
    ): Response<LaravelApiResponse<JsonElement>>

    @DELETE("favorites/{productId}")
    suspend fun deleteFavorite(
        @Path("productId") productId: String,
    ): Response<LaravelApiResponse<JsonElement>>
}

interface OrderApiService {
    @POST("checkout")
    suspend fun checkout(
        @Body body: CheckoutRequestDto,
    ): Response<LaravelApiResponse<JsonElement>>

    @GET("orders")
    suspend fun getOrders(): Response<LaravelApiResponse<JsonElement>>

    @GET("orders/{id}")
    suspend fun getOrder(
        @Path("id") orderId: String,
    ): Response<LaravelApiResponse<JsonElement>>
}

interface SellerOrderApiService {
    @GET("seller/orders")
    suspend fun getSellerOrders(): Response<LaravelApiResponse<JsonElement>>

    @GET("seller/orders/{id}")
    suspend fun getSellerOrder(
        @Path("id") orderId: String,
    ): Response<LaravelApiResponse<JsonElement>>

    @PUT("seller/orders/{id}/status")
    suspend fun updateSellerOrderStatus(
        @Path("id") orderId: String,
        @Body body: SellerOrderStatusUpdateRequestDto,
    ): Response<LaravelApiResponse<JsonElement>>
}

interface SellerProductApiService {
    @Multipart
    @POST("products")
    suspend fun createProduct(
        @PartMap body: Map<String, @JvmSuppressWildcards RequestBody>,
        @Part imageFile: MultipartBody.Part? = null,
    ): Response<LaravelApiResponse<JsonElement>>

    @Multipart
    @POST("products/{id}")
    suspend fun updateProduct(
        @Path("id") productId: String,
        @Header("X-HTTP-Method-Override") methodOverride: String = "PUT",
        @PartMap body: Map<String, @JvmSuppressWildcards RequestBody>,
        @Part imageFile: MultipartBody.Part? = null,
    ): Response<LaravelApiResponse<JsonElement>>

    @DELETE("products/{id}")
    suspend fun deleteProduct(
        @Path("id") productId: String,
    ): Response<LaravelApiResponse<JsonElement>>
}

interface SellerDashboardApiService {
    @GET("seller/dashboard")
    suspend fun getDashboard(): Response<LaravelApiResponse<JsonElement>>
}

interface SellerFinanceApiService {
    @GET("seller/finance")
    suspend fun getFinanceSummary(): Response<LaravelApiResponse<JsonElement>>
}

interface SellerVerificationApiService {
    @GET("seller/verification")
    suspend fun getSellerVerification(): Response<LaravelApiResponse<JsonElement>>

    @Multipart
    @POST("seller/verification")
    suspend fun submitSellerVerification(
        @PartMap body: Map<String, @JvmSuppressWildcards RequestBody>,
        @Part documents: List<MultipartBody.Part> = emptyList(),
    ): Response<LaravelApiResponse<JsonElement>>
}

interface ReviewApiService {
    @POST("reviews")
    suspend fun createReview(
        @Body body: Map<String, @JvmSuppressWildcards Any?>,
    ): Response<LaravelApiResponse<JsonElement>>
}
