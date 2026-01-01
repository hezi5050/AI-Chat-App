package com.hezi.chatsdk.core.config

/**
 * Represents an LLM provider with its name and available models.
 * Each provider implementation should create an instance with their specific details.
 */
data class Provider(
    val name: String,
    val models: List<String>
)

