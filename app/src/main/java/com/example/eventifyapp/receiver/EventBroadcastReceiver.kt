package com.example.eventifyapp.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

class EventBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {

        when (intent?.action) {

            Intent.ACTION_BOOT_COMPLETED -> {

                Toast.makeText(
                    context,
                    "Device berhasil dinyalakan",
                    Toast.LENGTH_SHORT
                ).show()

            }

            "EVENT_INTERESTED" -> {

                val title = intent.getStringExtra("title") ?: "Event"

                NotificationHelper.showNotification(
                    context,
                    "Eventify",
                    "Kamu tertarik pada $title"
                )

            }

        }

    }
}