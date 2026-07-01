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

    // Ambil 1 pesan terakhir per kontak unik, buat tampilan inbox/daftar percakapan
    @Query("""
        SELECT * FROM messages 
         WHERE id IN (
            SELECT id FROM (
                SELECT id, 
                       CASE WHEN senderEmail = :currentUserEmail THEN receiverEmail ELSE senderEmail END as contactEmail,
                       MAX(timestamp)
                FROM messages
                GROUP BY contactEmail
            )
        ) AND (senderEmail != :currentUserEmail OR receiverEmail != :currentUserEmail)
        ORDER BY timestamp DESC
    """)
    fun getLatestMessagePerSender(currentUserEmail: String): Flow<List<Message>>

    // Untuk tab "Unread" — sama tapi cuma yang belum dibaca dan dikirim oleh orang lain
    @Query("""
        SELECT * FROM messages 
        WHERE id IN (
            SELECT id FROM (
                SELECT id, 
                       CASE WHEN senderEmail = :currentUserEmail THEN receiverEmail ELSE senderEmail END as contactEmail,
                       MAX(timestamp)
                FROM messages
                GROUP BY contactEmail
            )
        ) AND isRead = 0 AND senderEmail != :currentUserEmail
        ORDER BY timestamp DESC
    """)
    fun getUnreadConversations(currentUserEmail: String): Flow<List<Message>>

    // Mengambil riwayat chat antara user saat ini dan kontak tertentu
    @Query("""
        SELECT * FROM messages 
        WHERE (senderEmail = :contactEmail AND receiverEmail = :currentUserEmail)
           OR (senderEmail = :currentUserEmail AND receiverEmail = :contactEmail)
        ORDER BY timestamp ASC
    """)
    fun getChatMessages(contactEmail: String, currentUserEmail: String): Flow<List<Message>>
}