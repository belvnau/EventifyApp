package com.example.eventifyapp.repository

import com.example.eventifyapp.dao.NotificationDao
import com.example.eventifyapp.model.NotificationItem
import kotlinx.coroutines.flow.Flow

class NotificationRepository(private val notificationDao: NotificationDao) {

    suspend fun insertNotification(notification: NotificationItem): Long =
        notificationDao.insertNotification(notification)

    suspend fun insertNotifications(notifications: List<NotificationItem>) =
        notificationDao.insertNotifications(notifications)

    suspend fun updateNotification(notification: NotificationItem) =
        notificationDao.updateNotification(notification)

    suspend fun deleteNotification(notification: NotificationItem) =
        notificationDao.deleteNotification(notification)

    fun getAllNotifications(): Flow<List<NotificationItem>> =
        notificationDao.getAllNotifications()

    fun getNotificationsByType(type: String): Flow<List<NotificationItem>> =
        notificationDao.getNotificationsByType(type)

    suspend fun markAsRead(notificationId: Long) =
        notificationDao.markAsRead(notificationId)

    suspend fun markAllAsReadByEvent(eventId: Long) =
        notificationDao.markAllAsReadByEvent(eventId)

    fun getUnreadCount(): Flow<Int> = notificationDao.getUnreadCount()

    suspend fun deleteOldNotifications(timestamp: Long) =
        notificationDao.deleteOldNotifications(timestamp)
}