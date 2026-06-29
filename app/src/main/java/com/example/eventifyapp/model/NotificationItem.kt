package com.example.eventifyapp.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notifications")
data class NotificationItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val message: String,
    val type: String, // "event", "message", "system"
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val eventId: Long? = null
)