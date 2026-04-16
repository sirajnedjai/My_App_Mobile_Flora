package com.example.myappmobile.data.repository

import android.content.Context
import android.util.Log
import com.example.myappmobile.data.local.dummy.DummyUsers
import com.example.myappmobile.data.local.room.DatabaseProvider
import com.example.myappmobile.data.local.room.entity.UserEntity
import com.example.myappmobile.domain.model.User
import com.example.myappmobile.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AuthRepositoryImpl : AuthRepository {
    private companion object {
        const val TAG = "REGISTER_DEBUG"
        const val PREFS_NAME = "flora_auth"
        const val KEY_CURRENT_USER = "current_user"
        const val FIELD_SEPARATOR = "\u001F"
    }

    private var appContext: Context? = null
    private val _currentUser = MutableStateFlow(guestUser())
    override val currentUser: StateFlow<User> = _currentUser.asStateFlow()
    private var currentPassword: String = "password123"

    fun initialize(context: Context) {
        appContext = context.applicationContext
        _currentUser.value = prefs().getString(KEY_CURRENT_USER, null)?.let(::decodeUser) ?: guestUser()
    }

    override suspend fun login(email: String, password: String): Result<User> {
        val normalizedEmail = email.trim().lowercase()
        val roomUser = runCatching {
            DatabaseProvider.getDatabase().userDao().getByEmailAndPassword(normalizedEmail, password)
        }.getOrNull()

        if (roomUser != null) {
            val mappedUser = roomUser.toDomainUser()
            currentPassword = password
            _currentUser.value = mappedUser
            persistCurrentUser(mappedUser)
            return Result.success(mappedUser)
        }

        val fallbackUser = when {
            (normalizedEmail == DummyUsers.seller.email.lowercase() || normalizedEmail == "bachir@flora.com") && password == "password123" -> DummyUsers.seller.copy(email = "bachir@flora.com")
            normalizedEmail == DummyUsers.elena.email.lowercase() && password == "password123" -> DummyUsers.elena
            normalizedEmail == DummyUsers.buyer2.email.lowercase() && password == "password123" -> DummyUsers.buyer2
            (normalizedEmail == DummyUsers.buyer.email.lowercase() || normalizedEmail == "baha@flora.com") && password == "password123" -> DummyUsers.buyer.copy(email = "baha@flora.com")
            else -> null
        }?.copy(email = email.trim(), isAuthenticated = true)

        return if (fallbackUser != null) {
            currentPassword = password
            _currentUser.value = fallbackUser
            persistCurrentUser(fallbackUser)
            Result.success(fallbackUser)
        } else {
            Result.failure(IllegalArgumentException("Invalid email or password."))
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
    ): Result<User> {
        val normalizedEmail = email.trim().lowercase()
        val userDao = DatabaseProvider.getDatabase().userDao()

        return runCatching {
            val existingUser = userDao.getByEmail(normalizedEmail)
            require(existingUser == null) { "An account with this email already exists." }

            userDao.insert(
                UserEntity(
                    name = fullName.trim(),
                    email = normalizedEmail,
                    password = password,
                    phone = phoneNumber.trim().ifBlank { null },
                    role = if (isSeller) "Seller" else "Customer",
                ),
            )
            val savedUser = checkNotNull(userDao.getByEmail(normalizedEmail)) {
                "Account was not saved. Please try again."
            }
            Log.d(
                TAG,
                "User saved to RoomDB: id=${savedUser.id}, email=${savedUser.email}, role=${savedUser.role}",
            )
            val domainUser = savedUser.toDomainUser()
            currentPassword = password
            _currentUser.value = domainUser
            domainUser
        }.fold(
            onSuccess = { savedUser -> Result.success(savedUser) },
            onFailure = { error -> Result.failure(error) },
        ).also { result ->
            result.getOrNull()?.let(::persistCurrentUser)
        }
    }

    override suspend fun updateCurrentUserProfile(
        fullName: String,
        email: String,
        phoneNumber: String,
    ): Result<User> {
        val current = _currentUser.value
        if (!current.isAuthenticated) {
            return Result.failure(IllegalStateException("No authenticated user found."))
        }

        val normalizedEmail = email.trim().lowercase()
        val trimmedName = fullName.trim()
        val trimmedPhone = phoneNumber.trim()

        return runCatching {
            val currentId = current.id.toLongOrNull()
            val userDao = DatabaseProvider.getDatabase().userDao()

            if (currentId != null) {
                val roomUser = userDao.getById(currentId)
                if (roomUser != null) {
                    val emailOwner = userDao.getByEmail(normalizedEmail)
                    require(emailOwner == null || emailOwner.id == currentId) {
                        "Another account already uses this email."
                    }
                    userDao.updateProfile(
                        id = currentId,
                        name = trimmedName,
                        email = normalizedEmail,
                        phone = trimmedPhone.ifBlank { null },
                    )
                }
            }

            current.copy(
                fullName = trimmedName,
                email = normalizedEmail,
                phone = trimmedPhone,
            )
        }.fold(
            onSuccess = { updatedUser ->
                _currentUser.value = updatedUser
                persistCurrentUser(updatedUser)
                Result.success(updatedUser)
            },
            onFailure = { error -> Result.failure(error) },
        )
    }

    override fun logout() {
        currentPassword = "password123"
        _currentUser.value = guestUser()
        prefs().edit().remove(KEY_CURRENT_USER).apply()
    }

    override suspend fun updateCurrentUserPassword(
        currentPassword: String,
        newPassword: String,
    ): Result<Unit> {
        val current = _currentUser.value
        if (!current.isAuthenticated) {
            return Result.failure(IllegalStateException("No authenticated user found."))
        }
        if (this.currentPassword != currentPassword) {
            return Result.failure(IllegalArgumentException("Current password is incorrect."))
        }

        return runCatching {
            val currentId = current.id.toLongOrNull()
            if (currentId != null) {
                DatabaseProvider.getDatabase().userDao().updatePassword(currentId, newPassword)
            }
            this.currentPassword = newPassword
        }
    }

    private fun UserEntity.toDomainUser(): User = User(
        id = id.toString(),
        fullName = name,
        email = email,
        phone = phone.orEmpty(),
        membershipTier = if (role.equals("Seller", ignoreCase = true)) {
            "MASTER CERAMICIST"
        } else {
            "PREMIUM MEMBER"
        },
        isAuthenticated = true,
        isSeller = role.equals("Seller", ignoreCase = true),
    )

    private fun guestUser(): User = User(
        id = "",
        fullName = "",
        email = "",
        isAuthenticated = false,
        isSeller = false,
    )

    private fun persistCurrentUser(user: User) {
        if (!user.isAuthenticated) {
            prefs().edit().remove(KEY_CURRENT_USER).apply()
            return
        }
        prefs().edit().putString(KEY_CURRENT_USER, encodeUser(user)).apply()
    }

    private fun encodeUser(user: User): String = listOf(
        user.id,
        user.fullName,
        user.email,
        user.phone,
        user.avatarUrl,
        user.membershipTier,
        user.isAuthenticated.toString(),
        user.isSeller.toString(),
    ).joinToString(FIELD_SEPARATOR)

    private fun decodeUser(raw: String): User {
        val fields = raw.split(FIELD_SEPARATOR)
        if (fields.size < 8) return guestUser()
        return User(
            id = fields[0],
            fullName = fields[1],
            email = fields[2],
            phone = fields[3],
            avatarUrl = fields[4],
            membershipTier = fields[5],
            isAuthenticated = fields[6].toBoolean(),
            isSeller = fields[7].toBoolean(),
        )
    }

    private fun prefs() = checkNotNull(appContext) {
        "AuthRepositoryImpl is not initialized. Call initialize(context) first."
    }.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
}
