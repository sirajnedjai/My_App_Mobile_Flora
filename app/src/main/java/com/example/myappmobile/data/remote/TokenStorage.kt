package com.example.myappmobile.data.remote

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class TokenStorage {
    private var appContext: Context? = null
    private var sharedPreferences: SharedPreferences? = null

    fun initialize(context: Context) {
        appContext = context.applicationContext
        sharedPreferences = buildPreferences(context.applicationContext)
    }

    fun getToken(): String = prefs().getString(KEY_AUTH_TOKEN, "").orEmpty()

    fun saveToken(token: String) {
        prefs().edit().putString(KEY_AUTH_TOKEN, token).apply()
    }

    fun clear() {
        prefs().edit().remove(KEY_AUTH_TOKEN).apply()
    }

    private fun prefs() = sharedPreferences ?: checkNotNull(appContext) {
        "TokenStorage is not initialized. Call initialize(context) first."
    }.let { context ->
        buildPreferences(context).also { sharedPreferences = it }
    }

    private fun buildPreferences(context: Context): SharedPreferences = runCatching {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
    }.getOrElse {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    private companion object {
        const val PREFS_NAME = "flora_api_session"
        const val KEY_AUTH_TOKEN = "auth_token"
    }
}
