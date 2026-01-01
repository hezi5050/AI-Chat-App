package com.hezi.chatsdk.provider_router

import com.hezi.chatsdk.core.config.Provider
import com.hezi.chatsdk.core.config.SdkConfiguration
import com.hezi.chatsdk.core.models.ChatRequest
import com.hezi.chatsdk.core.models.ChatResponse
import com.hezi.chatsdk.core.models.StreamEvent
import kotlinx.coroutines.flow.Flow

/**
 * Routes chat requests to the appropriate LLM provider based on configuration.
 */
interface ProviderRouter {

    /**
     * Update the SDK configuration
     */
    fun updateConfiguration(config: SdkConfiguration)

    /**
     * Update the SDK configuration using a builder lambda
     */
    fun updateConfiguration(block: SdkConfiguration.() -> SdkConfiguration)

    /**
     * Get the current SDK configuration
     */
    fun getConfiguration(): SdkConfiguration

    /**
     * Get all available providers
     */
    fun getAvailableProviders(): List<Provider>

    /**
     * Perform a chat request (non-streaming)
     */
    suspend fun chat(request: ChatRequest): ChatResponse

    /**
     * Perform a chat request with streaming
     */
    fun chatStream(request: ChatRequest): Flow<StreamEvent>

    /**
     * Get provider info by name
     */
    fun getProvider(providerName: String): Provider
}