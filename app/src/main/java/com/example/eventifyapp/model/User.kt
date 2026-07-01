package com.example.eventifyapp.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "users", indices = [Index(value = ["email"], unique = true)])
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    var name: String,
    var username: String = "",
    var email: String,
    var password: String = "",
    var bio: String = "",
    var location: String = "Jakarta, ID",
    var joinedDate: String = "Joined Mar 2024",
    var avatarUrl: String? = null
)

