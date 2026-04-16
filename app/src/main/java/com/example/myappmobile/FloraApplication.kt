package com.example.myappmobile

import android.app.Application
import com.example.myappmobile.core.di.AppContainer
import com.example.myappmobile.core.localization.LanguageManager
import com.example.myappmobile.data.local.room.DatabaseProvider

class FloraApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        DatabaseProvider.initialize(this)
        AppContainer.authRepository.initialize(this)
        AppContainer.uiPreferencesRepository.initialize(this)
        AppContainer.accountSettingsRepository.initialize(this)
        AppContainer.notificationRepository.initialize(this)
        AppContainer.localNotificationGateway.initialize(this)
        AppContainer.searchHistoryRepository.initialize(this)
        LanguageManager.applyLanguage(this, AppContainer.uiPreferencesRepository.languageCode.value)
        AppContainer.productReviewRepository.initialize(this)
    }
}
