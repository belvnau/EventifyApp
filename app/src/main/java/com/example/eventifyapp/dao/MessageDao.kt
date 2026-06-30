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

    // Baru: ambil 1 pesan terakhir per kontak unik, buat tampilan inbox/daftar percakapan
    @Query("""
        SELECT * FROM messages 
        WHERE timestamp IN (
            SELECT MAX(timestamp) FROM messages GROUP BY senderEmail
        )
        ORDER BY timestamp DESC
    """)
    fun getLatestMessagePerSender(): Flow<List<Message>>

    // Baru: untuk tab "Unread" — sama tapi cuma yang belum dibaca
    @Query("""
        SELECT * FROM messages 
        WHERE timestamp IN (
            SELECT MAX(timestamp) FROM messages GROUP BY senderEmail
        ) AND isRead = 0
        ORDER BY timestamp DESC
    """)
    fun getUnreadConversations(): Flow<List<Message>>
}