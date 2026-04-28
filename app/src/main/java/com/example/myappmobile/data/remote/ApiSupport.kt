package com.example.myappmobile.data.remote

import com.example.myappmobile.BuildConfig
import com.google.gson.Gson
import com.google.gson.JsonElement
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response as OkHttpResponse
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLException

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
            .addInterceptor(RetryInterceptor())
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(90, TimeUnit.SECONDS)
            .writeTimeout(90, TimeUnit.SECONDS)
            .callTimeout(120, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .apply {
                if (BuildConfig.DEBUG) {
                    addInterceptor(
                        HttpLoggingInterceptor().apply {
                            level = HttpLoggingInterceptor.Level.BODY
                            redactHeader("Authorization")
                        },
                    )
                }
            }
            .build()
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BackendUrlResolver.apiBaseUrl)
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

private class RetryInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): OkHttpResponse {
        val request = chain.request()
        val maxAttempts = if (request.isSafeToRetry()) 3 else 1
        var attempt = 0
        var lastException: IOException? = null

        while (attempt < maxAttempts) {
            attempt += 1

            try {
                val response = chain.proceed(request)
                if (!request.isSafeToRetry() || response.code !in RETRIABLE_STATUS_CODES || attempt >= maxAttempts) {
                    return response
                }
                response.close()
            } catch (exception: IOException) {
                lastException = exception
                if (!request.isSafeToRetry() || attempt >= maxAttempts || exception is java.io.InterruptedIOException) {
                    throw exception
                }
            }

            Thread.sleep(RETRY_BACKOFF_MS * attempt)
        }

        throw lastException ?: IOException("Request failed after retry attempts.")
    }

    private fun Request.isSafeToRetry(): Boolean = method in SAFE_RETRY_METHODS

    private companion object {
        val SAFE_RETRY_METHODS = setOf("GET", "HEAD", "OPTIONS")
        val RETRIABLE_STATUS_CODES = setOf(408, 425, 429, 500, 502, 503, 504)
        const val RETRY_BACKOFF_MS = 1_500L
    }
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
    is SocketTimeoutException -> ApiException(
        message = "The server is taking longer than expected to respond. Render may be waking up; please try again in a moment.",
    )
    is UnknownHostException -> ApiException(
        message = "Unable to reach the server. Check your internet connection and try again.",
    )
    is SSLException -> ApiException(
        message = "A secure connection to the server could not be established. Please try again.",
    )
    is IOException -> ApiException(
        message = "Unable to reach the server right now. Please try again shortly.",
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
        408 -> "The server took too long to respond. Please try again."
        429 -> "Too many requests were sent. Please wait a moment and try again."
        422 -> "Please review the submitted fields and try again."
        502, 503, 504 -> "The server is temporarily unavailable or waking up. Please retry in a moment."
        500 -> "The server returned an unexpected error. Please try again later."
        else -> "Request failed${statusCode?.let { " ($it)" }.orEmpty()}."
    }
}
