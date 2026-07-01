package com.example.eventifyapp.repository

import com.example.eventifyapp.dao.EventDao
import com.example.eventifyapp.model.Event
import kotlinx.coroutines.flow.Flow

class EventRepository(private val eventDao: EventDao) {

    suspend fun insertEvent(event: Event): Long = eventDao.insertEvent(event)

    suspend fun insertEvents(events: List<Event>) = eventDao.insertEvents(events)

    suspend fun updateEvent(event: Event) = eventDao.updateEvent(event)

    suspend fun deleteEvent(event: Event) = eventDao.deleteEvent(event)

    fun getAllEvents(): Flow<List<Event>> = eventDao.getAllEvents()

    suspend fun getEventById(eventId: Long): Event? = eventDao.getEventById(eventId)

    fun getEventsByCategory(category: String): Flow<List<Event>> =
        eventDao.getEventsByCategory(category)

    fun searchEvents(query: String): Flow<List<Event>> =
        eventDao.searchEvents(query)

    suspend fun toggleFavorite(eventId: Long, currentStatus: Boolean) {
        eventDao.updateFavoriteStatus(eventId, !currentStatus)
    }

    fun getFavoriteEvents(): Flow<List<Event>> = eventDao.getFavoriteEvents()
}