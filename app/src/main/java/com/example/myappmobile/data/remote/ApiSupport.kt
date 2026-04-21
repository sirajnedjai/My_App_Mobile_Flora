package com.example.myappmobile.data.remote

import com.example.myappmobile.BuildConfig
import com.google.gson.Gson
import com.google.gson.JsonElement
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response as OkHttpResponse
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class ApiException(
    override val message: String,
    val statusCode: Int? = null,
    val validationErrors: Map<String, List<String>> = emptyMap(),
) : Exception(message)

class AuthInterceptor(
    private val tokenStorage: TokenStorage,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): OkHttpResponse {
        val token = tokenStorage.getToken()
        val request = chain.request().newBuilder()
            .header("Accept", "application/json")
            .apply {
                if (token.isNotBlank()) {
                    header("Authorization", "Bearer $token")
                }
            }
            .build()
        return chain.proceed(request)
    }
}

class NetworkModule(
    tokenStorage: TokenStorage,
) {
    val gson: Gson = Gson()

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(tokenStorage))
            .apply {
                if (BuildConfig.DEBUG) {
                    addInterceptor(
                        HttpLoggingInterceptor().apply {
                            level = HttpLoggingInterceptor.Level.BODY
                        },
                    )
                }
            }
            .build()
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BuildConfig.FLORA_API_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    val authApiService: AuthApiService by lazy { retrofit.create(AuthApiService::class.java) }
    val productApiService: ProductApiService by lazy { retrofit.create(ProductApiService::class.java) }
    val storeApiService: StoreApiService by lazy { retrofit.create(StoreApiService::class.java) }
    val cartApiService: CartApiService by lazy { retrofit.create(CartApiService::class.java) }
    val favoriteApiService: FavoriteApiService by lazy { retrofit.create(FavoriteApiService::class.java) }
    val orderApiService: OrderApiService by lazy { retrofit.create(OrderApiService::class.java) }
    val sellerOrderApiService: SellerOrderApiService by lazy { retrofit.create(SellerOrderApiService::class.java) }
    val sellerProductApiService: SellerProductApiService by lazy { retrofit.create(SellerProductApiService::class.java) }
    val sellerDashboardApiService: SellerDashboardApiService by lazy { retrofit.create(SellerDashboardApiService::class.java) }
    val sellerFinanceApiService: SellerFinanceApiService by lazy { retrofit.create(SellerFinanceApiService::class.java) }
    val sellerVerificationApiService: SellerVerificationApiService by lazy { retrofit.create(SellerVerificationApiService::class.java) }
    val reviewApiService: ReviewApiService by lazy { retrofit.create(ReviewApiService::class.java) }
}

fun <T> Response<LaravelApiResponse<T>>.requireBody(gson: Gson): LaravelApiResponse<T> {
    val body = body()
    if (isSuccessful && body != null) {
        return body
    }

    val errorEnvelope = runCatching {
        errorBody()?.charStream()?.use { gson.fromJson(it, LaravelApiResponse::class.java) }
    }.getOrNull()
    val errors = (errorEnvelope?.errors as? Map<*, *>)?.mapNotNull { (key, value) ->
        val entries = (value as? List<*>)?.mapNotNull { it?.toString() }.orEmpty()
        key?.toString()?.let { it to entries }
    }?.toMap().orEmpty()

    throw ApiException(
        message = buildApiMessage(
            statusCode = code(),
            envelopeMessage = errorEnvelope?.message,
            validationErrors = errors,
        ),
        statusCode = code(),
        validationErrors = errors,
    )
}

fun Throwable.toApiException(): ApiException = when (this) {
    is ApiException -> this
    is UnknownHostException, is SocketTimeoutException, is IOException -> ApiException(
        message = "Unable to reach the server. Check Wi-Fi and confirm the backend is running at ${BuildConfig.FLORA_API_BASE_URL.removeSuffix("/")}.",
    )
    else -> ApiException(message = message ?: "Something went wrong while contacting the server.")
}

fun extractDataElement(payload: JsonElement?): JsonElement? {
    if (payload == null || payload.isJsonNull) return null
    if (payload.isJsonArray) return payload
    val obj = payload.asObjectOrNull() ?: return payload
    return obj.element("data", "items", "products", "product", "user", "order", "orders", "cart", "favorites", "favorite", "reviews")
        ?: payload
}

private fun buildApiMessage(
    statusCode: Int?,
    envelopeMessage: String?,
    validationErrors: Map<String, List<String>>,
): String {
    if (!envelopeMessage.isNullOrBlank()) return envelopeMessage
    if (validationErrors.isNotEmpty()) {
        return validationErrors.values.flatten().firstOrNull().orEmpty().ifBlank {
            "Please review the submitted fields and try again."
        }
    }
    return when (statusCode) {
        401 -> "Your session has expired. Please sign in again."
        404 -> "This feature is not available on the current server route. Please verify the backend endpoint configuration."
        422 -> "Please review the submitted fields and try again."
        500 -> "The server returned an unexpected error. Please try again later."
        else -> "Request failed${statusCode?.let { " ($it)" }.orEmpty()}."
    }
}
