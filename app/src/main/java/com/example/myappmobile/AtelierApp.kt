package com.example.myappmobile

import android.app.Activity
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.LayoutDirection
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.myappmobile.core.di.AppContainer
import com.example.myappmobile.core.localization.LanguageManager
import com.example.myappmobile.core.navigation.Routes
import com.example.myappmobile.core.navigation.AppNavGraph
import com.example.myappmobile.core.theme.FloraTheme
import com.google.firebase.messaging.FirebaseMessaging

@Composable
fun AtelierApp() {
    val isDarkMode by AppContainer.uiPreferencesRepository.isDarkMode.collectAsState()
    val languageCode by AppContainer.uiPreferencesRepository.languageCode.collectAsState()
    val currentUser by AppContainer.authRepository.currentUser.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        Log.d(TAG, "AtelierApp composed. isAuthenticated=${currentUser.isAuthenticated} isSeller=${currentUser.isSeller}")
    }

    LaunchedEffect(languageCode) {
        val currentLanguage = context.resources.configuration.locales[0]?.language.orEmpty()
        if (currentLanguage != languageCode) {
            LanguageManager.applyLanguage(context, languageCode)
            (context as? Activity)?.recreate()
        }
    }

    LaunchedEffect(currentUser.id, currentUser.isAuthenticated) {
        if (currentUser.isAuthenticated) {
            runCatching {
                FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
                    if (token.isNotBlank()) {
                        AppContainer.notificationRepository.updateCurrentDeviceToken(token)
                        AppContainer.notificationRepository.registerCurrentDevice(currentUser.id)
                        AppContainer.notificationBackendApi.registerDeviceToken(
                            userId = currentUser.id,
                            token = token,
                        )
                    }
                }
            }
        }
    }

    FloraTheme(darkTheme = isDarkMode) {
        val navController = rememberNavController()
        val backStackEntry by navController.currentBackStackEntryAsState()
        val route = backStackEntry?.destination?.route
        LaunchedEffect(route) {
            Log.d(TAG, "AtelierApp current route=${route.orEmpty()}")
        }
        val enforceEnglish = route == Routes.LOGIN || route == Routes.REGISTER
        val layoutDirection = if (!enforceEnglish && LanguageManager.isRightToLeft(languageCode)) {
            LayoutDirection.Rtl
        } else {
            LayoutDirection.Ltr
        }
        CompositionLocalProvider(
            androidx.compose.ui.platform.LocalLayoutDirection provides layoutDirection,
        ) {
            AppNavGraph(navController = navController)
        }
    }
}

private const val TAG = "FloraStartup"
