package com.hezi.chatsdk.core.models

import com.hezi.chatsdk.core.config.Provider

data class ChatResponse(
    val text: String,
    val provider: Provider,
    val model: String,
    val latencyMs: Long,
    val tokenUsage: TokenUsage? = null
)

data class TokenUsage(
    val promptTokens: Int,
    val completionTokens: Int,
    val totalTokens: Int
)

