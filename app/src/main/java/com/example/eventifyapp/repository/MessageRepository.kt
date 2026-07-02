package com.example.eventifyapp.repository

import com.example.eventifyapp.dao.MessageDao
import com.example.eventifyapp.model.Message
import kotlinx.coroutines.flow.Flow

class MessageRepository(private val messageDao: MessageDao) {

    suspend fun insertMessage(message: Message): Long = messageDao.insertMessage(message)

    suspend fun updateMessage(message: Message) = messageDao.updateMessage(message)

    suspend fun deleteMessage(message: Message) = messageDao.deleteMessage(message)

    fun getAllMessages(): Flow<List<Message>> = messageDao.getAllMessages()

    fun getMessagesBySender(email: String): Flow<List<Message>> =
        messageDao.getMessagesBySender(email)

    suspend fun markAsRead(messageId: Long) = messageDao.markAsRead(messageId)

    fun getUnreadCount(): Flow<Int> = messageDao.getUnreadCount()

    fun getLatestMessagePerSender(currentUserEmail: String): Flow<List<Message>> = 
        messageDao.getLatestMessagePerSender(currentUserEmail)

    fun getUnreadConversations(currentUserEmail: String): Flow<List<Message>> = 
        messageDao.getUnreadConversations(currentUserEmail)

    fun getChatMessages(contactEmail: String, currentUserEmail: String): Flow<List<Message>> =
        messageDao.getChatMessages(contactEmail, currentUserEmail)

    fun getLatestAllMessages(currentUserEmail: String): Flow<List<Message>> =
        messageDao.getLatestAllMessages(currentUserEmail)

    fun getLatestCommunityMessages(currentUserEmail: String): Flow<List<Message>> =
        messageDao.getLatestCommunityMessages(currentUserEmail)
}