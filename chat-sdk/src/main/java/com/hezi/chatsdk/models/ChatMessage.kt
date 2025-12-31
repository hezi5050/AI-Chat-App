package com.hezi.chatsdk.models

data class ChatMessage(
    val role: MessageRole,
    val content: String
)

enum class MessageRole {
    USER,
    ASSISTANT,
    SYSTEM
}

