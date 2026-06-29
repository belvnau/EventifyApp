package com.example.eventifyapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.eventifyapp.repository.EventRepository
import com.example.eventifyapp.repository.NotificationRepository

class ViewModelFactory(
    private val eventRepository: EventRepository? = null,
    private val notificationRepository: NotificationRepository? = null
) : ViewModelProvider.Factory {
    
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(EventViewModel::class.java) -> {
                EventViewModel(eventRepository ?: throw IllegalArgumentException("EventRepository required")) as T
            }
            modelClass.isAssignableFrom(NotificationViewModel::class.java) -> {
                NotificationViewModel(notificationRepository ?: throw IllegalArgumentException("NotificationRepository required")) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
