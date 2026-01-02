package com.hezi.aichatapp.data.repository

import com.hezi.aichatapp.data.local.dao.ConversationDao
import com.hezi.aichatapp.data.local.dao.MessageDao
import com.hezi.aichatapp.data.local.entity.ConversationEntity
import com.hezi.aichatapp.data.local.entity.MessageEntity
import com.hezi.aichatapp.ui.chat.UiMessage
import com.hezi.aichatapp.ui.chat.UiMessageType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConversationRepositoryImpl @Inject constructor(
    private val conversationDao: ConversationDao,
    private val messageDao: MessageDao
) : ConversationRepository {
    
    override fun getAllConversations(): Flow<List<Conversation>> {
        return conversationDao.getAllConversations().map { entities ->
            entities.map { entity ->
                val messageCount = messageDao.getMessagesByConversationId(entity.id)
                    .first()
                    .size
                
                entity.toConversation(messageCount)
            }
        }
    }
    
    override suspend fun getConversationWithMessages(conversationId: Long): ConversationWithMessages? {
        val conversationEntity = conversationDao.getConversationById(conversationId) ?: return null
        val messageEntities = messageDao.getMessagesByConversationId(conversationId).first()
        
        val conversation = conversationEntity.toConversation(messageEntities.size)
        val messages = messageEntities.map { it.toUiMessage() }
        
        return ConversationWithMessages(conversation, messages)
    }
    
    override suspend fun createConversation(title: String, providerName: String, model: String): Long {
        val timestamp = System.currentTimeMillis()
        val conversation = ConversationEntity(
            title = title,
            createdAt = timestamp,
            updatedAt = timestamp,
            providerName = providerName,
            model = model
        )
        return conversationDao.insertConversation(conversation)
    }
    
    override suspend fun saveMessage(conversationId: Long, message: UiMessage) {
        val messageEntity = MessageEntity(
            conversationId = conversationId,
            type = message.type.name,
            content = message.content,
            timestamp = message.timestamp
        )
        messageDao.insertMessage(messageEntity)
        
        // Update conversation timestamp
        updateConversationTimestamp(conversationId)
    }
    
    override suspend fun deleteConversation(conversationId: Long) {
        val conversation = conversationDao.getConversationById(conversationId)
        conversation?.let {
            conversationDao.deleteConversation(it)
        }
    }
    
    override suspend fun updateConversationTimestamp(conversationId: Long) {
        val conversation = conversationDao.getConversationById(conversationId) ?: return
        val updatedConversation = conversation.copy(updatedAt = System.currentTimeMillis())
        conversationDao.updateConversation(updatedConversation)
    }
    
    // Extension functions for mapping entities to domain models
    private fun ConversationEntity.toConversation(messageCount: Int): Conversation {
        return Conversation(
            id = id,
            title = title,
            createdAt = createdAt,
            updatedAt = updatedAt,
            providerName = providerName,
            model = model,
            messageCount = messageCount
        )
    }
    
    private fun MessageEntity.toUiMessage(): UiMessage {
        return UiMessage(
            id = id.toString(),
            type = UiMessageType.valueOf(type),
            content = content,
            timestamp = timestamp,
            isStreaming = false
        )
    }
}

