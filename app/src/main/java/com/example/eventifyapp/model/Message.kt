package com.example.eventifyapp.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "messages")
data class Message(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val senderName: String,
    val senderEmail: String,
    val receiverEmail: String = "",
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val isCommunity: Boolean = false,
    val eventId: Long = 0,
    val groupTitle: String = ""
)