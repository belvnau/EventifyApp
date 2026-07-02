package com.example.eventifyapp.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.eventifyapp.R
import com.example.eventifyapp.activities.NotificationActivity

object NotificationHelper {

    private const val CHANNEL_ID = "eventify_channel"
    private const val CHANNEL_NAME = "Eventify Notifications"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifikasi dari Eventify"
            }
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    fun sendInterestedNotification(context: Context, eventTitle: String) {
        // Intent: klik notif → buka NotificationActivity
        val intent = Intent(context, NotificationActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Event Ditambahkan!")
            .setContentText("Kamu tertarik pada $eventTitle")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true) // notif hilang otomatis setelah diklik
            .build()

        // Cek permission dulu (wajib untuk Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                NotificationManagerCompat.from(context).notify(
                    System.currentTimeMillis().toInt(),
                    notification
                )
            }
        } else {
            NotificationManagerCompat.from(context).notify(
                System.currentTimeMillis().toInt(),
                notification
            )
        }
    }
}