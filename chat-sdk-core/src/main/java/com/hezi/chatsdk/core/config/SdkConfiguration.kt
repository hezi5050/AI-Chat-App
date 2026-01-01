package com.hezi.chatsdk.core.config

/**
 * Configuration for the AI Chat SDK.
 * 
 * @param providerName The LLM providerName to use (e.g., "openai", "mock")
 * @param model The specific model to use (e.g., "gpt-4", "gpt-3.5-turbo")
 * @param temperature Controls randomness in responses (0.0 = deterministic, 2.0 = very random)
 * @param maxTokens Maximum number of tokens to generate in the response
 */
data class SdkConfiguration(
    val providerName: String = "Mock",
    val model: String = "default",
    val temperature: Float = 0.7f,
    val maxTokens: Int = 500
)

