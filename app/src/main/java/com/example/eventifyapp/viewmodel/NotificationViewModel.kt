package com.example.eventifyapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eventifyapp.model.NotificationItem
import com.example.eventifyapp.repository.NotificationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NotificationViewModel(private val repository: NotificationRepository) : ViewModel() {

    private val _notifications = MutableStateFlow<List<NotificationItem>>(emptyList())
    val notifications: StateFlow<List<NotificationItem>> = _notifications.asStateFlow()

    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadNotifications()
        loadUnreadCount()
    }

    fun loadNotifications() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.getAllNotifications().collect { items ->
                    _notifications.value = items
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadUnreadCount() {
        viewModelScope.launch {
            repository.getUnreadCount().collect { count ->
                _unreadCount.value = count
            }
        }
    }

    suspend fun addNotification(notification: NotificationItem): Long {
        return try {
            val id = repository.insertNotification(notification)
            loadUnreadCount()
            id
        } catch (e: Exception) {
            _error.value = e.message
            -1
        }
    }

    suspend fun markAsRead(notificationId: Long) {
        try {
            repository.markAsRead(notificationId)
            loadUnreadCount()
        } catch (e: Exception) {
            _error.value = e.message
        }
    }

    suspend fun deleteNotification(notification: NotificationItem) {
        try {
            repository.deleteNotification(notification)
        } catch (e: Exception) {
            _error.value = e.message
        }
    }

    suspend fun clearOldNotifications() {
        try {
            val oneWeekAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000)
            repository.deleteOldNotifications(oneWeekAgo)
            loadNotifications()
        } catch (e: Exception) {
            _error.value = e.message
        }
    }

    fun getNotificationsByType(type: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.getNotificationsByType(type).collect { items ->
                    _notifications.value = items
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteAllNotifications() {
        viewModelScope.launch {
            try {
                repository.deleteAllNotifications()
                loadUnreadCount()
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}