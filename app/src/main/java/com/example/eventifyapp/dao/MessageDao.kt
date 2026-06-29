package com.example.eventifyapp.dao

import androidx.room.*
import com.example.eventifyapp.model.Message
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {
    @Insert
    suspend fun insertMessage(message: Message): Long

    @Update
    suspend fun updateMessage(message: Message)

    @Delete
    suspend fun deleteMessage(message: Message)

    @Query("SELECT * FROM messages ORDER BY timestamp DESC")
    fun getAllMessages(): Flow<List<Message>>

    @Query("SELECT * FROM messages WHERE senderEmail = :email ORDER BY timestamp DESC")
    fun getMessagesBySender(email: String): Flow<List<Message>>

    @Query("UPDATE messages SET isRead = 1 WHERE id = :messageId")
    suspend fun markAsRead(messageId: Long)

    @Query("SELECT COUNT(*) FROM messages WHERE isRead = 0")
    fun getUnreadCount(): Flow<Int>
}