package com.example.eventifyapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.eventifyapp.repository.EventRepository
import com.example.eventifyapp.repository.NotificationRepository
import com.example.eventifyapp.repository.ReviewRepository
import com.example.eventifyapp.repository.MessageRepository

class ViewModelFactory(
    private val eventRepository: EventRepository? = null,
    private val notificationRepository: NotificationRepository? = null,
    private val reviewRepository: ReviewRepository? = null,
    private val messageRepository: MessageRepository? = null
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
            modelClass.isAssignableFrom(ReviewViewModel::class.java) -> {
                ReviewViewModel(reviewRepository ?: throw IllegalArgumentException("ReviewRepository required")) as T
            }
            modelClass.isAssignableFrom(MessageViewModel::class.java) -> {
                MessageViewModel(messageRepository ?: throw IllegalArgumentException("MessageRepository required")) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}