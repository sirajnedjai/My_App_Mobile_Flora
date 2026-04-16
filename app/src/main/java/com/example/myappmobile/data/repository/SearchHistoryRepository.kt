package com.example.myappmobile.data.repository

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SearchHistoryRepository {
    private var appContext: Context? = null

    private val _history = MutableStateFlow<List<String>>(emptyList())
    val history: StateFlow<List<String>> = _history.asStateFlow()

    fun initialize(context: Context) {
        appContext = context.applicationContext
        _history.value = decodeHistory(prefs().getString(KEY_HISTORY, null))
    }

    fun addSearch(query: String) {
        val normalized = query.trim()
        if (normalized.isBlank()) return
        val updated = listOf(normalized) + _history.value.filterNot { it.equals(normalized, ignoreCase = true) }
        persist(updated.take(MAX_HISTORY))
    }

    fun removeSearch(query: String) {
        persist(_history.value.filterNot { it.equals(query, ignoreCase = true) })
    }

    fun clearHistory() {
        persist(emptyList())
    }

    private fun persist(values: List<String>) {
        prefs().edit().putString(KEY_HISTORY, encodeHistory(values)).apply()
        _history.value = values
    }

    private fun prefs() = requireNotNull(appContext) {
        "SearchHistoryRepository is not initialized."
    }.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private fun encodeHistory(values: List<String>): String =
        values.joinToString(SEPARATOR, transform = ::escape)

    private fun decodeHistory(raw: String?): List<String> {
        if (raw.isNullOrBlank()) return emptyList()
        return raw.split(SEPARATOR)
            .map(::unescape)
            .filter { it.isNotBlank() }
            .take(MAX_HISTORY)
    }

    private fun escape(value: String): String =
        value.replace("\\", "\\\\").replace(SEPARATOR, ESCAPED_SEPARATOR)

    private fun unescape(value: String): String =
        value.replace(ESCAPED_SEPARATOR, SEPARATOR).replace("\\\\", "\\")

    private companion object {
        const val PREFS_NAME = "flora_search_history"
        const val KEY_HISTORY = "recent_searches"
        const val MAX_HISTORY = 10
        const val SEPARATOR = "||"
        const val ESCAPED_SEPARATOR = "\\|\\|"
    }
}
