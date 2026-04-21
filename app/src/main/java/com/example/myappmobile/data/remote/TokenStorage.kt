package com.example.myappmobile.data.remote

import android.content.Context

class TokenStorage {
    private var appContext: Context? = null

    fun initialize(context: Context) {
        appContext = context.applicationContext
    }

    fun getToken(): String = prefs().getString(KEY_AUTH_TOKEN, "").orEmpty()

    fun saveToken(token: String) {
        prefs().edit().putString(KEY_AUTH_TOKEN, token).apply()
    }

    fun clear() {
        prefs().edit().remove(KEY_AUTH_TOKEN).apply()
    }

    private fun prefs() = checkNotNull(appContext) {
        "TokenStorage is not initialized. Call initialize(context) first."
    }.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private companion object {
        const val PREFS_NAME = "flora_api_session"
        const val KEY_AUTH_TOKEN = "auth_token"
    }
}
