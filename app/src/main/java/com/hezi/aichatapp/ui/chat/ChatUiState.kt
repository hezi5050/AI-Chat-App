package com.hezi.aichatapp.ui.chat

import com.hezi.chatsdk.core.models.MessageRole

/**
 * Represents a message in the UI layer
 */
data class UiMessage(
    val id: String,
    val role: MessageRole,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isStreaming: Boolean = false
)

/**
 * UI state for the chat screen
 */
data class ChatUiState(
    val messages: List<UiMessage> = emptyList(),
    val inputText: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isStreaming: Boolean = false
)

