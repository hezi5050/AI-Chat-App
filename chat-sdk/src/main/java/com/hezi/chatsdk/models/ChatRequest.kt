package com.hezi.chatsdk.models

data class ChatRequest(
    val messages: List<ChatMessage>,
    val temperature: Float = 0.7f,
    val maxTokens: Int = 500
)

