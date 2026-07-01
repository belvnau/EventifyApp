package com.example.eventifyapp.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "events")
data class Event(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String,
    val location: String,
    val date: String,
    val time: String,
    val price: String,
    val category: String,
    val imageUrl: String = "",
    val organizer: String = "",
    val isFavorite: Boolean = false,
    val isJoined: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)