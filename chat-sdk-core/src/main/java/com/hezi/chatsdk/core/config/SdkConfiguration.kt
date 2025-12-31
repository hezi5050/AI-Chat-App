package com.hezi.chatsdk.core.config

data class SdkConfiguration(
    val provider: Provider = Provider.OPENAI,
    val model: String = "gpt-4o-mini",
    val temperature: Float = 0.7f,
    val maxTokens: Int = 500
)

