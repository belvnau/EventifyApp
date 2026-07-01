package com.example.eventifyapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eventifyapp.model.Event
import com.example.eventifyapp.repository.EventRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class EventViewModel(private val repository: EventRepository) : ViewModel() {

    private val _allEvents = MutableStateFlow<List<Event>>(emptyList())
    val allEvents: StateFlow<List<Event>> = _allEvents.asStateFlow()

    private val _favoriteEvents = MutableStateFlow<List<Event>>(emptyList())
    val favoriteEvents: StateFlow<List<Event>> = _favoriteEvents.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private var currentEvents: List<Event> = emptyList()

    init {
        loadAllEvents()
        loadFavoriteEvents()
    }

    fun loadAllEvents() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.getAllEvents().collect { events ->
                    currentEvents = events
                    _allEvents.value = events
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadFavoriteEvents() {
        viewModelScope.launch {
            repository.getFavoriteEvents().collect { events ->
                _favoriteEvents.value = events
            }
        }
    }

    suspend fun addEvent(event: Event): Long {
        return try {
            repository.insertEvent(event)
        } catch (e: Exception) {
            _error.value = e.message
            -1
        }
    }

    suspend fun updateEvent(event: Event) {
        try {
            repository.updateEvent(event)
        } catch (e: Exception) {
            _error.value = e.message
        }
    }

    suspend fun deleteEvent(event: Event) {
        try {
            repository.deleteEvent(event)
        } catch (e: Exception) {
            _error.value = e.message
        }
    }

    suspend fun getEventById(eventId: Long): Event? {
        return try {
            repository.getEventById(eventId)
        } catch (e: Exception) {
            _error.value = e.message
            null
        }
    }

    fun searchEvents(query: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                if (query.isEmpty()) {
                    _allEvents.value = currentEvents
                } else {
                    repository.searchEvents(query).collect { events ->
                        _allEvents.value = events
                    }
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    suspend fun toggleFavorite(eventId: Long, currentStatus: Boolean) {
        try {
            repository.toggleFavorite(eventId, currentStatus)
            loadFavoriteEvents()
        } catch (e: Exception) {
            _error.value = e.message
        }
    }

    suspend fun toggleJoin(eventId: Long, currentStatus: Boolean) {
        try {
            repository.updateJoinedStatus(eventId, !currentStatus)
            loadAllEvents()
        } catch (e: Exception) {
            _error.value = e.message
        }
    }

    fun clearError() {
        _error.value = null
    }
}