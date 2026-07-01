package com.example.eventifyapp.dao

import androidx.room.*
import com.example.eventifyapp.model.NotificationItem
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationDao {
    @Insert
    suspend fun insertNotification(notification: NotificationItem): Long

    @Insert
    suspend fun insertNotifications(notifications: List<NotificationItem>)

    @Update
    suspend fun updateNotification(notification: NotificationItem)

    @Delete
    suspend fun deleteNotification(notification: NotificationItem)

    @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
    fun getAllNotifications(): Flow<List<NotificationItem>>

    @Query("SELECT * FROM notifications WHERE type = :type ORDER BY timestamp DESC")
    fun getNotificationsByType(type: String): Flow<List<NotificationItem>>

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :notificationId")
    suspend fun markAsRead(notificationId: Long)

    @Query("UPDATE notifications SET isRead = 1 WHERE eventId = :eventId")
    suspend fun markAllAsReadByEvent(eventId: Long)

    @Query("SELECT COUNT(*) FROM notifications WHERE isRead = 0")
    fun getUnreadCount(): Flow<Int>

    @Query("DELETE FROM notifications WHERE timestamp < :timestamp")
    suspend fun deleteOldNotifications(timestamp: Long)

    @Query("DELETE FROM notifications")
    suspend fun deleteAllNotifications()
}