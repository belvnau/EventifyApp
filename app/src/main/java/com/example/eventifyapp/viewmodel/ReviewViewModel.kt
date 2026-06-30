package com.example.eventifyapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eventifyapp.model.Review
import com.example.eventifyapp.repository.ReviewRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ReviewViewModel(private val repository: ReviewRepository) : ViewModel() {

    private val _reviews = MutableStateFlow<List<Review>>(emptyList())
    val reviews: StateFlow<List<Review>> = _reviews.asStateFlow()

    private val _averageRating = MutableStateFlow(0f)
    val averageRating: StateFlow<Float> = _averageRating.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun loadReviews(eventId: Long) {
        viewModelScope.launch {
            try {
                repository.getReviewsByEvent(eventId).collect { list ->
                    _reviews.value = list
                }
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun loadAverageRating(eventId: Long) {
        viewModelScope.launch {
            try {
                repository.getAverageRating(eventId).collect { avg ->
                    _averageRating.value = avg ?: 0f
                }
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    suspend fun addReview(review: Review): Long {
        return try {
            repository.insertReview(review)
        } catch (e: Exception) {
            _error.value = e.message
            -1
        }
    }

    fun clearError() {
        _error.value = null
    }
}