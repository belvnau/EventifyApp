package com.example.eventifyapp.dao

import androidx.room.*
import com.example.eventifyapp.model.Review
import kotlinx.coroutines.flow.Flow

@Dao
interface ReviewDao {
    @Insert
    suspend fun insertReview(review: Review): Long

    @Update
    suspend fun updateReview(review: Review)

    @Delete
    suspend fun deleteReview(review: Review)

    @Query("SELECT * FROM reviews WHERE eventId = :eventId ORDER BY timestamp DESC")
    fun getReviewsByEvent(eventId: Long): Flow<List<Review>>

    @Query("SELECT AVG(rating) FROM reviews WHERE eventId = :eventId")
    fun getAverageRating(eventId: Long): Flow<Float?>
}