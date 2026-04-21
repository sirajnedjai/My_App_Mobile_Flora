package com.example.myappmobile

import android.app.Application
import android.util.Log
import com.example.myappmobile.core.di.AppContainer
import com.example.myappmobile.core.localization.LanguageManager
import com.example.myappmobile.data.local.room.DatabaseProvider

class FloraApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Log.e(TAG, "Uncaught exception on thread=${thread.name}", throwable)
            defaultHandler?.uncaughtException(thread, throwable)
        }
        Log.d(TAG, "Application.onCreate reached")
        DatabaseProvider.initialize(this)
        Log.d(TAG, "DatabaseProvider initialized")
        AppContainer.initializeContext(this)
        Log.d(TAG, "AppContainer context initialized")
        AppContainer.authRepository.initialize(this)
        Log.d(TAG, "AuthRepository initialized")
        AppContainer.uiPreferencesRepository.initialize(this)
        Log.d(TAG, "UiPreferencesRepository initialized")
        AppContainer.accountSettingsRepository.initialize(this)
        Log.d(TAG, "AccountSettingsRepository initialized")
        AppContainer.notificationRepository.initialize(this)
        Log.d(TAG, "NotificationRepository initialized")
        AppContainer.localNotificationGateway.initialize(this)
        Log.d(TAG, "LocalNotificationGateway initialized")
        AppContainer.searchHistoryRepository.initialize(this)
        Log.d(TAG, "SearchHistoryRepository initialized")
        LanguageManager.applyLanguage(this, AppContainer.uiPreferencesRepository.languageCode.value)
        Log.d(TAG, "LanguageManager applied language=${AppContainer.uiPreferencesRepository.languageCode.value}")
        AppContainer.productReviewRepository.initialize(this)
        Log.d(TAG, "ProductReviewRepository initialized")
    }

    private companion object {
        const val TAG = "FloraStartup"
    }
}
