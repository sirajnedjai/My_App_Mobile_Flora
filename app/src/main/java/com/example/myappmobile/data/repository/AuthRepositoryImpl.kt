package com.example.myappmobile.data.repository

import android.util.Log
import android.content.Context
import com.example.myappmobile.data.remote.ApiException
import com.example.myappmobile.data.remote.AuthApiService
import com.example.myappmobile.data.remote.AuthPayloadDto
import com.example.myappmobile.data.remote.ChangePasswordRequestDto
import com.example.myappmobile.data.remote.LoginRequestDto
import com.example.myappmobile.data.remote.RegisterRequestDto
import com.example.myappmobile.data.remote.TokenStorage
import com.example.myappmobile.data.remote.UpdateProfileRequestDto
import com.example.myappmobile.data.remote.UserDto
import com.example.myappmobile.data.remote.asBooleanOrNull
import com.example.myappmobile.data.remote.asObjectOrNull
import com.example.myappmobile.data.remote.asStringOrNull
import com.example.myappmobile.data.remote.requireBody
import com.example.myappmobile.data.remote.string
import com.example.myappmobile.data.remote.toApiException
import com.example.myappmobile.domain.model.User
import com.example.myappmobile.domain.repository.AuthRepository
import com.google.gson.Gson
import com.google.gson.JsonElement
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthRepositoryImpl(
    private val authApiService: AuthApiService,
    private val tokenStorage: TokenStorage,
    private val gson: Gson,
) : AuthRepository {
    private var appContext: Context? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _currentUser = MutableStateFlow(guestUser())
    override val currentUser: StateFlow<User> = _currentUser.asStateFlow()
    private var currentPassword: String = ""

    fun initialize(context: Context) {
        appContext = context.applicationContext
        tokenStorage.initialize(context)
        _currentUser.value = prefs().getString(KEY_CURRENT_USER, null)?.let(::decodeUser) ?: guestUser()

        if (tokenStorage.getToken().isNotBlank()) {
            scope.launch {
                refreshCurrentUser()
            }
        }
    }

    override suspend fun login(email: String, password: String): Result<User> = runCatching {
        val response = authApiService.login(
            LoginRequestDto(
                email = email.trim(),
                password = password,
            ),
        ).requireBody(gson)

        val data = response.data
            ?: throw ApiException(response.message ?: "Login completed without a response payload.")
        val payload = parseAuthPayload(data)
        val token = payload.first
        val user = payload.second.copy(isAuthenticated = true)

        require(token.isNotBlank()) { "The server did not return an authentication token." }

        currentPassword = password
        tokenStorage.saveToken(token)
        _currentUser.value = user
        persistCurrentUser(user)
        user
    }.fold(
        onSuccess = { Result.success(it) },
        onFailure = { Result.failure(it.toApiException()) },
    )

    override fun logout() {
        val hadToken = tokenStorage.getToken().isNotBlank()
        currentPassword = ""
        tokenStorage.clear()
        clearPersistedUser()
        _currentUser.value = guestUser()

        if (hadToken) {
            scope.launch {
                runCatching { authApiService.logout().requireBody(gson) }
            }
        }
    }

    override suspend fun register(
        fullName: String,
        email: String,
        password: String,
        phoneNumber: String,
        isSeller: Boolean,
        storeName: String,
        address: String,
        postalCode: String,
    ): Result<User> = runCatching {
        val resolvedRole = if (isSeller) SELLER_ROLE else BUYER_ROLE
        val requestBody = RegisterRequestDto(
            name = fullName.trim(),
            email = email.trim(),
            password = password,
            passwordConfirmation = password,
            phone = phoneNumber.trim(),
            role = resolvedRole,
            isSeller = isSeller,
            storeName = storeName.trim().ifBlank { null },
            storeAddress = address.trim().ifBlank { null },
            postalCode = postalCode.trim().ifBlank { null },
        )
        Log.d(TAG, "Registration request body: ${gson.toJson(requestBody)}")
        val response = authApiService.register(
            requestBody,
        ).requireBody(gson)

        val payload = response.data
        when {
            payload == null || payload.isJsonNull -> User(
                id = "",
                fullName = fullName.trim(),
                email = email.trim(),
                phone = phoneNumber.trim(),
                isAuthenticated = false,
                isSeller = isSeller,
            )
            payload.asObjectOrNull()?.has("user") == true || payload.asObjectOrNull()?.has("token") == true -> {
                parseAuthPayload(payload).second.copy(isAuthenticated = false)
            }
            else -> parseUser(payload).copy(isAuthenticated = false)
        }
    }.fold(
        onSuccess = { Result.success(it) },
        onFailure = { Result.failure(it.toApiException()) },
    )

    override suspend fun updateCurrentUserProfile(
        fullName: String,
        email: String,
        phoneNumber: String,
        address: String,
        avatarUrl: String,
    ): Result<User> {
        val current = _currentUser.value
        if (!current.isAuthenticated) {
            return Result.failure(IllegalStateException("No authenticated user found."))
        }

        val request = UpdateProfileRequestDto(
            fullName = fullName.trim(),
            email = email.trim(),
            phone = phoneNumber.trim(),
            address = address.trim().ifBlank { null },
        )

        return runCatching {
            updateProfileOnBackend(request)
            val updatedUser = current.copy(
                fullName = request.fullName,
                email = request.email,
                phone = request.phone,
                avatarUrl = avatarUrl.trim().ifBlank { current.avatarUrl },
            )
            _currentUser.value = updatedUser
            persistCurrentUser(updatedUser)
            updatedUser
        }.fold(
            onSuccess = { Result.success(it) },
            onFailure = { Result.failure(it.toApiException()) },
        )
    }

    override suspend fun updateCurrentUserPassword(
        currentPassword: String,
        newPassword: String,
        confirmPassword: String,
    ): Result<Unit> {
        val current = _currentUser.value
        if (!current.isAuthenticated) {
            return Result.failure(IllegalStateException("No authenticated user found."))
        }
        return runCatching {
            authApiService.changePassword(
                ChangePasswordRequestDto(
                    currentPassword = currentPassword,
                    password = newPassword,
                    passwordConfirmation = confirmPassword,
                ),
            ).requireBody(gson)
            this.currentPassword = newPassword
            refreshCurrentUser()
            Unit
        }.fold(
            onSuccess = { Result.success(Unit) },
            onFailure = { Result.failure(it.toApiException()) },
        )
    }

    private suspend fun refreshCurrentUser(): Result<User> = runCatching {
        val response = authApiService.me().requireBody(gson)
        val payload = response.data ?: throw ApiException(response.message ?: "Missing current user payload.")
        val user = parseUser(payload).copy(isAuthenticated = true)
        _currentUser.value = user
        persistCurrentUser(user)
        user
    }.onFailure {
        tokenStorage.clear()
        clearPersistedUser()
        _currentUser.value = guestUser()
    }

    private suspend fun updateProfileOnBackend(request: UpdateProfileRequestDto) {
        authApiService.updateProfile(request).requireBody(gson)
    }

    private fun parseAuthPayload(data: JsonElement): Pair<String, User> {
        val dataObject = data.asObjectOrNull()
        val payload = gson.fromJson(data, AuthPayloadDto::class.java)
        val nestedUser = dataObject?.get("user")
        val token = payload.token
            ?: dataObject?.string("token", "access_token", "accessToken", "plainTextToken")
            .orEmpty()
        val user = when {
            payload.user != null -> payload.user.toDomainUser()
            nestedUser != null -> parseUser(nestedUser)
            else -> parseUser(data)
        }
        return token to user
    }

    private fun parseUser(data: JsonElement): User {
        val dataObject = data.asObjectOrNull()
        val effectivePayload = dataObject?.get("user") ?: data
        val dto = gson.fromJson(effectivePayload, UserDto::class.java)
        val obj = effectivePayload.asObjectOrNull()
        val role = dto.role.orEmpty()
        val isSeller = dto.isSeller
            ?: obj?.get("seller")?.asBooleanOrNull()
            ?: role.equals("seller", ignoreCase = true)

        return dto.toDomainUser().copy(
            fullName = dto.name.orEmpty().ifBlank { obj?.string("name", "full_name", "fullname") ?: "FLORA User" },
            email = dto.email.orEmpty().ifBlank { obj?.string("email") ?: "" },
            phone = dto.phone.orEmpty().ifBlank { obj?.string("phone", "phone_number").orEmpty() },
            avatarUrl = dto.avatarUrl.orEmpty().ifBlank { obj?.string("avatar", "avatar_url", "profile_photo_url", "image").orEmpty() },
            isAuthenticated = true,
            isSeller = isSeller,
            membershipTier = if (isSeller) "MASTER CERAMICIST" else "PREMIUM MEMBER",
        )
    }

    private fun UserDto.toDomainUser(): User = User(
        id = id.asStringOrNull().orEmpty(),
        fullName = name.orEmpty(),
        email = email.orEmpty(),
        phone = phone.orEmpty(),
        avatarUrl = avatarUrl.orEmpty(),
        membershipTier = if (isSeller == true || role.equals("seller", ignoreCase = true)) {
            "MASTER CERAMICIST"
        } else {
            "PREMIUM MEMBER"
        },
        isAuthenticated = true,
        isSeller = isSeller == true || role.equals("seller", ignoreCase = true),
    )

    private fun guestUser(): User = User(
        id = "",
        fullName = "",
        email = "",
        isAuthenticated = false,
        isSeller = false,
    )

    private fun persistCurrentUser(user: User) {
        prefs().edit().putString(KEY_CURRENT_USER, encodeUser(user)).apply()
    }

    private fun clearPersistedUser() {
        prefs().edit().remove(KEY_CURRENT_USER).apply()
    }

    private fun encodeUser(user: User): String = gson.toJson(user)

    private fun decodeUser(raw: String): User = runCatching {
        gson.fromJson(raw, User::class.java)
    }.getOrElse {
        guestUser()
    }

    private fun prefs() = checkNotNull(appContext) {
        "AuthRepositoryImpl is not initialized. Call initialize(context) first."
    }.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private companion object {
        const val TAG = "AuthRepository"
        const val PREFS_NAME = "flora_auth"
        const val KEY_CURRENT_USER = "current_user"
        const val BUYER_ROLE = "buyer"
        const val SELLER_ROLE = "seller"
    }
}
