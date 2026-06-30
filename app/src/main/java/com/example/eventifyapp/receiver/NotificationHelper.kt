package com.example.eventifyapp.receiver

import android.annotation.SuppressLint
import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.eventifyapp.R

object NotificationHelper {

    private const val CHANNEL_ID = "event_channel"

    @SuppressLint("MissingPermission")
    fun showNotification(
        context: Context,
        title: String,
        message: String
    ) {

        createChannel(context)

        val builder = NotificationCompat.Builder(
            context,
            CHANNEL_ID
        )
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

            if (
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {

                NotificationManagerCompat
                    .from(context)
                    .notify(System.currentTimeMillis().toInt(), builder.build())

            }

        } else {

            NotificationManagerCompat
                .from(context)
                .notify(System.currentTimeMillis().toInt(), builder.build())

        }

    }

    private fun createChannel(context: Context) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val channel = NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT
            )

            channel.description = context.getString(R.string.notification_channel_description)

            val manager = context.getSystemService(NotificationManager::class.java)

            manager.createNotificationChannel(channel)

        }

    }

}