package com.example.eventifyapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eventifyapp.model.Message
import com.example.eventifyapp.repository.MessageRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MessageViewModel(private val repository: MessageRepository) : ViewModel() {

    private val _conversations = MutableStateFlow<List<Message>>(emptyList())
    val conversations: StateFlow<List<Message>> = _conversations.asStateFlow()

    private val _chatMessages = MutableStateFlow<List<Message>>(emptyList())
    val chatMessages: StateFlow<List<Message>> = _chatMessages.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // Dipakai MessagesActivity, tab "All"
    fun loadAllConversations(currentUserEmail: String) {
        viewModelScope.launch {
            try {
                repository.getLatestMessagePerSender(currentUserEmail).collect { list ->
                    _conversations.value = list
                }
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    // Dipakai MessagesActivity, tab "Unread"
    fun loadUnreadConversations(currentUserEmail: String) {
        viewModelScope.launch {
            try {
                repository.getUnreadConversations(currentUserEmail).collect { list ->
                    _conversations.value = list
                }
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    // Dipakai ChatDetailActivity, ambil semua pesan dengan 1 kontak tertentu
    fun loadChatWithSender(email: String, currentUserEmail: String) {
        viewModelScope.launch {
            try {
                repository.getChatMessages(email, currentUserEmail).collect { list ->
                    _chatMessages.value = list
                }
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    suspend fun sendMessage(message: Message): Long {
        return try {
            repository.insertMessage(message)
        } catch (e: Exception) {
            _error.value = e.message
            -1
        }
    }

    suspend fun markAsRead(messageId: Long) {
        try {
            repository.markAsRead(messageId)
        } catch (e: Exception) {
            _error.value = e.message
        }
    }

    // Dipakai MessagesActivity, tab All (private + circle chats)
    fun loadLatestAllMessages(currentUserEmail: String) {
        viewModelScope.launch {
            try {
                repository.getLatestAllMessages(currentUserEmail).collect { list ->
                    _conversations.value = list
                }
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    // Dipakai MessagesActivity, tab Community
    fun loadLatestCommunityMessages(currentUserEmail: String) {
        viewModelScope.launch {
            try {
                repository.getLatestCommunityMessages(currentUserEmail).collect { list ->
                    _conversations.value = list
                }
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }
}