package com.example.myappmobile.data.repository

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.myappmobile.MainActivity
import com.example.myappmobile.R
import com.example.myappmobile.domain.model.NotificationType

class AndroidLocalNotificationGateway {
    private var appContext: Context? = null

    fun initialize(context: Context) {
        appContext = context.applicationContext
        ensureChannel()
    }

    fun sendDeliveredNotification(
        title: String,
        body: String,
        orderId: String,
    ): Result<Unit> = sendNotification(
        title = title,
        body = body,
        orderId = orderId,
        type = NotificationType.ORDER_DELIVERED,
    )

    fun sendNotification(
        title: String,
        body: String,
        orderId: String,
        type: NotificationType,
    ): Result<Unit> = runCatching {
        val context = requireContext()
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("flora_destination", "notifications")
            putExtra("flora_order_id", orderId)
            putExtra("flora_notification_type", type.name)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            "$orderId:${type.name}".hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            return Result.failure(IllegalStateException("Notification permission has not been granted."))
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.flora_logo_round)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        NotificationManagerCompat.from(context).notify("$orderId:${type.name}".hashCode(), notification)
    }

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (manager.getNotificationChannel(CHANNEL_ID) != null) return
        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_ID,
                "Order updates",
                NotificationManager.IMPORTANCE_HIGH,
            ).apply {
                description = "Delivery and order progress updates from FLORA."
            },
        )
    }

    private fun requireContext(): Context = checkNotNull(appContext) {
        "AndroidLocalNotificationGateway is not initialized."
    }

    private companion object {
        const val CHANNEL_ID = "flora_order_updates"
    }
}
