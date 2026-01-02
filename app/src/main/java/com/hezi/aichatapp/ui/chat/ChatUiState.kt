package com.hezi.aichatapp.ui.chat

/**
 * Represents the type of message in the UI
 */
enum class UiMessageType {
    /**
     * Message from the user
     */
    USER,
    
    /**
     * Response from the AI assistant
     */
    ASSISTANT,
    
    /**
     * System message (commands, notifications, errors)
     */
    SYSTEM
}

/**
 * Represents a message in the UI layer
 * Uses app-specific UiMessageType instead of SDK's MessageRole
 */
data class UiMessage(
    val id: String,
    val type: UiMessageType,
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


