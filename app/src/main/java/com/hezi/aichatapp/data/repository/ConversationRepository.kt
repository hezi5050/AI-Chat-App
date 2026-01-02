package com.hezi.aichatapp.data.repository

import com.hezi.aichatapp.ui.chat.UiMessage
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for managing conversation persistence
 */
interface ConversationRepository {
    /**
     * Get all conversations ordered by last updated timestamp
     */
    fun getAllConversations(): Flow<List<Conversation>>
    
    /**
     * Get a specific conversation with its messages
     */
    suspend fun getConversationWithMessages(conversationId: Long): ConversationWithMessages?
    
    /**
     * Create a new conversation
     * @return The ID of the newly created conversation
     */
    suspend fun createConversation(title: String, providerName: String, model: String): Long
    
    /**
     * Save a message to a conversation
     */
    suspend fun saveMessage(conversationId: Long, message: UiMessage)
    
    /**
     * Delete a conversation and all its messages
     */
    suspend fun deleteConversation(conversationId: Long)
    
    /**
     * Update the conversation's last updated timestamp
     */
    suspend fun updateConversationTimestamp(conversationId: Long)
}

/**
 * Represents a conversation summary
 */
data class Conversation(
    val id: Long,
    val title: String,
    val createdAt: Long,
    val updatedAt: Long,
    val providerName: String,
    val model: String,
    val messageCount: Int = 0
)

/**
 * Represents a conversation with all its messages
 */
data class ConversationWithMessages(
    val conversation: Conversation,
    val messages: List<UiMessage>
)

