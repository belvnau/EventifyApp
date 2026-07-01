package com.example.eventifyapp.dao

import androidx.room.*
import com.example.eventifyapp.model.Event
import kotlinx.coroutines.flow.Flow

@Dao
interface EventDao {
    @Insert
    suspend fun insertEvent(event: Event): Long

    @Insert
    suspend fun insertEvents(events: List<Event>)

    @Update
    suspend fun updateEvent(event: Event)

    @Delete
    suspend fun deleteEvent(event: Event)

    @Query("SELECT * FROM events ORDER BY date ASC")
    fun getAllEvents(): Flow<List<Event>>

    @Query("SELECT * FROM events WHERE id = :eventId")
    suspend fun getEventById(eventId: Long): Event?

    @Query("SELECT * FROM events WHERE category = :category")
    fun getEventsByCategory(category: String): Flow<List<Event>>

    @Query("SELECT * FROM events WHERE title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%'")
    fun searchEvents(query: String): Flow<List<Event>>

    @Query("UPDATE events SET isFavorite = :isFavorite WHERE id = :eventId")
    suspend fun updateFavoriteStatus(eventId: Long, isFavorite: Boolean)

    @Query("SELECT * FROM events WHERE isFavorite = 1")
    fun getFavoriteEvents(): Flow<List<Event>>

    @Query("SELECT COUNT(*) FROM events")
    suspend fun getEventCount(): Int
}