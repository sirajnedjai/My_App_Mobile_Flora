package com.example.myappmobile.core.localization

import android.content.Context
import android.content.res.Configuration
import android.os.LocaleList
import java.util.Locale

enum class AppLanguage(val code: String) {
    ARABIC("ar"),
    ENGLISH("en"),
    FRENCH("fr");

    companion object {
        fun fromCode(code: String): AppLanguage = entries.firstOrNull { it.code == code } ?: ENGLISH
    }
}

object LanguageManager {
    val supportedLanguages: List<AppLanguage> = AppLanguage.entries

    fun applyLanguage(context: Context, languageCode: String) {
        val safeLanguageCode = AppLanguage.fromCode(languageCode).code
        val locale = Locale.forLanguageTag(safeLanguageCode)
        Locale.setDefault(locale)

        val resources = context.resources
        val configuration = Configuration(resources.configuration).apply {
            setLocale(locale)
            setLocales(LocaleList(locale))
            setLayoutDirection(locale)
        }

        @Suppress("DEPRECATION")
        resources.updateConfiguration(configuration, resources.displayMetrics)
        context.createConfigurationContext(configuration)
    }

    fun isRightToLeft(languageCode: String): Boolean {
        return AppLanguage.fromCode(languageCode) == AppLanguage.ARABIC
    }
}
