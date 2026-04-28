package com.example.myappmobile.data.repository

import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import android.content.Context
import com.example.myappmobile.data.remote.ApiException
import com.example.myappmobile.data.remote.AuthApiService
import com.example.myappmobile.data.remote.AuthPayloadDto
import com.example.myappmobile.data.remote.BackendUrlResolver
import com.example.myappmobile.data.remote.ChangePasswordRequestDto
import com.example.myappmobile.data.remote.LoginRequestDto
import com.example.myappmobile.data.remote.RegisterRequestDto
import com.example.myappmobile.data.remote.TokenStorage
import com.example.myappmobile.data.remote.UpdateProfileRequestDto
import com.example.myappmobile.data.remote.UserDto
import com.example.myappmobile.data.remote.asBooleanOrNull
import com.example.myappmobile.data.remote.asObjectOrNull
import com.example.myappmobile.data.remote.asStringOrNull
import com.example.myappmobile.data.remote.boolean
import com.example.myappmobile.data.remote.objectAt
import com.example.myappmobile.data.remote.requireBody
import com.example.myappmobile.data.remote.string
import com.example.myappmobile.data.remote.toApiException
import com.example.myappmobile.domain.model.SellerApprovalStatus
import com.example.myappmobile.domain.model.User
import com.example.myappmobile.domain.repository.AuthRepository
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

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
        _currentUser.value = prefs().getString(KEY_CURRENT_USER, null)?.let(::decodeUser)?.sanitized() ?: guestUser()

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
        val sanitizedUser = user.sanitized()
        _currentUser.value = sanitizedUser
        persistCurrentUser(sanitizedUser)
        sanitizedUser
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
        storeName: String,
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
            storeName = storeName.trim().ifBlank { null },
        )

        return runCatching {
            val backendUser = updateProfileOnBackend(
                request = request,
                avatarUrl = avatarUrl,
            )
            val updatedUser = backendUser ?: current.copy(
                fullName = request.fullName,
                email = request.email,
                phone = request.phone,
                address = request.address.orEmpty(),
                avatarUrl = avatarUrl.trim().ifBlank { current.avatarUrl },
                storeName = request.storeName.orEmpty().ifBlank { current.storeName },
            )
            val sanitizedUser = updatedUser.sanitized()
            _currentUser.value = sanitizedUser
            persistCurrentUser(sanitizedUser)
            sanitizedUser
        }.fold(
            onSuccess = { Result.success(it) },
            onFailure = { error ->
                val apiError = error.toApiException()
                if (apiError.statusCode == 401) logout()
                Result.failure(apiError)
            },
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
            onFailure = { error ->
                val apiError = error.toApiException()
                if (apiError.statusCode == 401) logout()
                Result.failure(apiError)
            },
        )
    }

    override suspend fun refreshCurrentUser(): Result<User> = runCatching {
        val response = authApiService.me().requireBody(gson)
        val payload = response.data ?: throw ApiException(response.message ?: "Missing current user payload.")
        val user = parseUser(payload).copy(isAuthenticated = true).sanitized()
        _currentUser.value = user
        persistCurrentUser(user)
        user
    }.onFailure {
        tokenStorage.clear()
        clearPersistedUser()
        _currentUser.value = guestUser()
    }

    private suspend fun updateProfileOnBackend(
        request: UpdateProfileRequestDto,
        avatarUrl: String,
    ): User? {
        val avatarUri = avatarUrl.trim()
        val response = if (avatarUri.startsWith("content://") || avatarUri.startsWith("file://")) {
            authApiService.updateProfileMultipart(
                body = request.toPartMap(),
                avatar = buildAvatarPart(avatarUri),
            ).requireBody(gson)
        } else {
            authApiService.updateProfile(request).requireBody(gson)
        }
        return response.data?.let(::parseUser)
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
        val storeObject = obj?.objectAt("store")
            ?: obj?.objectAt("seller")?.objectAt("store")
            ?: dto.store?.asObjectOrNull()
            ?: dto.seller?.asObjectOrNull()?.objectAt("store")
        val profileObject = obj?.objectAt("profile") ?: dto.profile?.asObjectOrNull()
        val role = dto.role.orEmpty().ifBlank {
            resolveRole(obj = obj, isSeller = dto.isSeller)
        }
        val hasSellerObject = dto.seller?.asObjectOrNull() != null || obj?.objectAt("seller") != null
        val isSeller = when {
            dto.isSeller != null -> dto.isSeller
            role.equals("seller", ignoreCase = true) -> true
            role.equals("buyer_seller", ignoreCase = true) -> true
            role.equals("buyer-seller", ignoreCase = true) -> true
            hasSellerObject -> true
            else -> false
        }
        val verificationStatus = resolveVerificationStatus(
            primary = obj,
            secondary = profileObject,
            tertiary = storeObject,
        )
        val sellerApprovalStatus = if (isSeller) {
            resolveVerificationStatus(
                primary = storeObject ?: obj,
                secondary = obj?.objectAt("seller"),
                tertiary = profileObject,
            )
        } else {
            SellerApprovalStatus.NOT_VERIFIED
        }
        val storeName = dto.storeName.orEmpty().ifBlank {
            obj?.string("store_name", "shop_name")
                ?: storeObject?.string("name", "store_name", "shop_name")
                ?: ""
        }
        val avatarUrl = BackendUrlResolver.normalizeImageUrl(
            dto.avatarUrl.orEmpty().ifBlank {
                firstNonBlank(
                    obj?.string("avatar", "avatar_url", "profile_photo_url", "profile_picture", "profile_picture_url", "image", "photo"),
                    profileObject?.string("avatar", "avatar_url", "profile_photo_url", "profile_picture", "profile_picture_url", "image", "photo"),
                    storeObject?.string("logo", "logo_url", "avatar", "avatar_url", "profile_picture", "profile_picture_url", "image"),
                ).orEmpty()
            },
        )

        return dto.toDomainUser().copy(
            fullName = dto.name.orEmpty().ifBlank { obj?.string("name", "full_name", "fullname") ?: "FLORA User" },
            email = dto.email.orEmpty().ifBlank { obj?.string("email") ?: "" },
            phone = dto.phone.orEmpty().ifBlank { obj?.string("phone", "phone_number").orEmpty() },
            address = dto.address.orEmpty().ifBlank {
                obj?.string("address", "street_address")
                    ?: profileObject?.string("address", "street_address")
                    ?: storeObject?.string("address", "store_address")
                    ?: ""
            },
            avatarUrl = avatarUrl,
            role = normalizedRole(role = role, isSeller = isSeller, storeName = storeName),
            storeName = storeName,
            verificationStatus = verificationStatus,
            sellerApprovalStatus = sellerApprovalStatus,
            isAuthenticated = true,
            isSeller = isSeller,
            membershipTier = if (isSeller) "SELLER ACCOUNT" else "BUYER ACCOUNT",
        )
    }

    private fun UserDto.toDomainUser(): User = User(
        id = id.asStringOrNull().orEmpty(),
        fullName = name.orEmpty(),
        email = email.orEmpty(),
        phone = phone.orEmpty(),
        address = address.orEmpty(),
        avatarUrl = BackendUrlResolver.normalizeImageUrl(avatarUrl),
        role = role.orEmpty(),
        storeName = storeName.orEmpty(),
        verificationStatus = when {
            isVerified == true -> SellerApprovalStatus.APPROVED
            else -> SellerApprovalStatus.NOT_VERIFIED
        },
        sellerApprovalStatus = when {
            approved == true -> SellerApprovalStatus.APPROVED
            else -> SellerApprovalStatus.NOT_VERIFIED
        },
        membershipTier = if (isSeller == true || role.equals("seller", ignoreCase = true)) {
            "SELLER ACCOUNT"
        } else {
            "BUYER ACCOUNT"
        },
        isAuthenticated = true,
        isSeller = isSeller == true ||
            role.equals("seller", ignoreCase = true) ||
            role.equals("buyer_seller", ignoreCase = true) ||
            role.equals("buyer-seller", ignoreCase = true) ||
            seller?.asObjectOrNull() != null,
    )

    private fun guestUser(): User = User(
        id = "",
        fullName = "",
        email = "",
        isAuthenticated = false,
        isSeller = false,
    )

    private fun persistCurrentUser(user: User) {
        prefs().edit().putString(KEY_CURRENT_USER, encodeUser(user.sanitized())).apply()
    }

    private fun clearPersistedUser() {
        prefs().edit().remove(KEY_CURRENT_USER).apply()
    }

    private fun encodeUser(user: User): String = gson.toJson(user)

    private fun decodeUser(raw: String): User = runCatching {
        val obj = gson.fromJson(raw, JsonObject::class.java)
        User(
            id = obj.string("id").orEmpty(),
            fullName = obj.string("fullName", "name", "full_name").orEmpty(),
            email = obj.string("email").orEmpty(),
            phone = obj.string("phone", "phoneNumber").orEmpty(),
            address = obj.string("address").orEmpty(),
            avatarUrl = BackendUrlResolver.normalizeImageUrl(obj.string("avatarUrl", "avatar", "avatar_url")),
            role = obj.string("role").orEmpty(),
            storeName = obj.string("storeName", "store_name").orEmpty(),
            verificationStatus = obj.toApprovalStatus("verificationStatus", "verification_status", "account_status"),
            sellerApprovalStatus = obj.toApprovalStatus("sellerApprovalStatus", "seller_approval_status", "approved"),
            membershipTier = obj.string("membershipTier").orEmpty(),
            isAuthenticated = obj.boolean("isAuthenticated").orFalse(),
            isSeller = obj.boolean("isSeller", "seller").orFalse(),
        )
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
        val AVATAR_PART_NAMES = listOf("avatar", "profile_photo", "profile_picture", "image", "photo")
    }

    private fun resolveVerificationStatus(
        primary: JsonObject?,
        secondary: JsonObject? = null,
        tertiary: JsonObject? = null,
    ): SellerApprovalStatus {
        val candidates = listOfNotNull(primary, secondary, tertiary)
        val resolved = candidates.firstNotNullOfOrNull { candidate ->
            resolveSellerApprovalStatusOrNull(candidate, includeGenericPendingFallback = true)
        }
        return resolved ?: SellerApprovalStatus.NOT_VERIFIED
    }

    private fun resolveRole(
        obj: JsonObject?,
        isSeller: Boolean?,
    ): String = when {
        obj?.string("role", "user_type", "account_type").isNullOrBlank().not() -> obj?.string("role", "user_type", "account_type").orEmpty()
        isSeller == true -> SELLER_ROLE
        else -> BUYER_ROLE
    }

    private fun normalizedRole(
        role: String,
        isSeller: Boolean,
        storeName: String,
    ): String {
        val normalized = role.trim().lowercase()
        return when {
            normalized.contains("buyer") && normalized.contains("seller") -> "buyer-seller"
            normalized.isNotBlank() -> normalized
            isSeller && storeName.isNotBlank() -> "seller"
            isSeller -> "seller"
            else -> "buyer"
        }
    }

    private fun firstNonBlank(vararg values: String?): String? = values.firstOrNull { !it.isNullOrBlank() }

    private fun UpdateProfileRequestDto.toPartMap(): Map<String, RequestBody> = buildMap {
        put("name", fullName.toPlainTextRequestBody())
        put("email", email.toPlainTextRequestBody())
        put("phone", phone.toPlainTextRequestBody())
        address?.takeIf(String::isNotBlank)?.let { put("address", it.toPlainTextRequestBody()) }
        storeName?.takeIf(String::isNotBlank)?.let { put("store_name", it.toPlainTextRequestBody()) }
    }

    private fun String.toPlainTextRequestBody(): RequestBody = toRequestBody("text/plain".toMediaTypeOrNull())

    private fun buildAvatarPart(avatarUri: String): MultipartBody.Part? {
        if (avatarUri.isBlank()) return null
        val context = checkNotNull(appContext) { "AuthRepositoryImpl is not initialized. Call initialize(context) first." }
        val uri = Uri.parse(avatarUri)
        val mimeType = context.contentResolver.getType(uri)?.takeIf { it.isNotBlank() } ?: "image/*"
        val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            ?: throw ApiException("Unable to read the selected profile image.")
        val fileName = context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
            ?.use { cursor ->
                val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index >= 0 && cursor.moveToFirst()) cursor.getString(index) else null
            }
            .orEmpty()
            .ifBlank { "profile_image" }
        val requestBody = bytes.toRequestBody(mimeType.toMediaTypeOrNull())
        return MultipartBody.Part.createFormData(AVATAR_PART_NAMES.first(), fileName, requestBody)
    }

    private fun User.sanitized(): User {
        val safeRole = role.takeIf { it.isNotBlank() } ?: if (isSeller) SELLER_ROLE else BUYER_ROLE
        return copy(
            id = id.orEmpty(),
            fullName = fullName.orEmpty(),
            email = email.orEmpty(),
            phone = phone.orEmpty(),
            address = address.orEmpty(),
            avatarUrl = BackendUrlResolver.normalizeImageUrl(avatarUrl),
            role = safeRole,
            storeName = storeName.orEmpty(),
            verificationStatus = safeApprovalStatus(verificationStatus),
            sellerApprovalStatus = safeApprovalStatus(sellerApprovalStatus),
            membershipTier = membershipTier.orEmpty().ifBlank {
                if (isSeller) "SELLER ACCOUNT" else "BUYER ACCOUNT"
            },
        )
    }

    private fun JsonObject.toApprovalStatus(vararg keys: String): SellerApprovalStatus {
        val direct = keys.firstNotNullOfOrNull { key ->
            get(key)?.asStringOrNull()?.let(::parseApprovalStatus)
        }
        if (direct != null) return direct
        return when {
            keys.any { get(it)?.asBooleanOrNull() == true } -> SellerApprovalStatus.APPROVED
            else -> SellerApprovalStatus.NOT_VERIFIED
        }
    }

    private fun safeApprovalStatus(status: SellerApprovalStatus): SellerApprovalStatus = status

    private fun parseApprovalStatus(raw: String): SellerApprovalStatus = when (raw.trim().lowercase()) {
        "approved", "verified", "active", "true", "1" -> SellerApprovalStatus.APPROVED
        "pending", "submitted", "under_review", "in_review", "awaiting_review" -> SellerApprovalStatus.PENDING
        "rejected", "declined", "denied" -> SellerApprovalStatus.REJECTED
        "unknown" -> SellerApprovalStatus.UNKNOWN
        else -> SellerApprovalStatus.NOT_VERIFIED
    }

    private fun Boolean?.orFalse(): Boolean = this ?: false
}
