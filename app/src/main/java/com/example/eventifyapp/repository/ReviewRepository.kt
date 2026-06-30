package com.example.eventifyapp.repository

import com.example.eventifyapp.dao.ReviewDao
import com.example.eventifyapp.model.Review
import kotlinx.coroutines.flow.Flow

class ReviewRepository(private val reviewDao: ReviewDao) {

    suspend fun insertReview(review: Review): Long = reviewDao.insertReview(review)

    suspend fun updateReview(review: Review) = reviewDao.updateReview(review)

    suspend fun deleteReview(review: Review) = reviewDao.deleteReview(review)

    fun getReviewsByEvent(eventId: Long): Flow<List<Review>> =
        reviewDao.getReviewsByEvent(eventId)

    fun getAverageRating(eventId: Long): Flow<Float?> =
        reviewDao.getAverageRating(eventId)
}