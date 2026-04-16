package com.example.myappmobile

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.content.ContextCompat
import com.example.myappmobile.core.di.AppContainer
import com.example.myappmobile.core.localization.LanguageManager
import com.example.myappmobile.domain.model.AppNotification
import com.example.myappmobile.domain.model.NotificationType
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class MainActivity : ComponentActivity() {
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LanguageManager.applyLanguage(this, AppContainer.uiPreferencesRepository.languageCode.value)
        enableEdgeToEdge()
        requestNotificationPermissionIfNeeded()
        handleNotificationIntent(intent)

        setContent {
            AtelierApp()
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleNotificationIntent(intent)
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            return
        }
        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }

    private fun handleNotificationIntent(intent: android.content.Intent?) {
        val extras = intent?.extras ?: return
        val orderId = extras.getString("orderId")
            ?: extras.getString("flora_order_id")
            ?: extras.getString("gcm.notification.orderId")
            ?: extras.getString("google.c.a.orderId")
            ?: ""
        if (orderId.isNotBlank()) {
            AppContainer.notificationNavigationRepository.setPendingOrderNavigation(orderId)
            val currentUser = AppContainer.authRepository.currentUser.value
            if (currentUser.id.isNotBlank()) {
                val title = extras.getString("gcm.notification.title")
                    ?: extras.getString("title")
                    ?: "Order Delivered"
                val body = extras.getString("gcm.notification.body")
                    ?: extras.getString("body")
                    ?: "Your FLORA order has been delivered."
                AppContainer.notificationRepository.saveNotification(
                    AppNotification(
                        id = "notification_${orderId}_tap",
                        userId = currentUser.id,
                        title = title,
                        body = body,
                        type = NotificationType.ORDER_DELIVERED,
                        relatedOrderId = orderId,
                        createdAt = LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMM dd, yyyy • HH:mm")),
                    ),
                )
            }
        }
    }
}
