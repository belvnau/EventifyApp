package com.example.eventifyapp.database

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Long? {
        return value
    }

    @TypeConverter
    fun dateToTimestamp(date: String?): String? {
        return date
    }
}