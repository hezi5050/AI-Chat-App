package com.hezi.chatsdk

import com.hezi.chatsdk.core.config.Provider
import com.hezi.chatsdk.core.config.SdkConfiguration
import com.hezi.chatsdk.core.models.ChatRequest
import com.hezi.chatsdk.core.models.ChatResponse
import com.hezi.chatsdk.core.models.StreamEvent
import com.hezi.chatsdk.di.SdkConfig
import com.hezi.chatsdk.provider_router.ProviderRouter
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Main SDK interface that aggregates all provider implementations.
 * This is the primary entry point for applications using the Chat SDK.
 */
@Singleton
class AiChatSdk @Inject constructor(
    private val router: ProviderRouter,
    private val sdkConfig: SdkConfig
) {
    
    /**
     * Enable or disable debug logging.
     * 
     * @param isDebug Whether to enable debug logging
     */
    fun setDebugMode(isDebug: Boolean) {
        sdkConfig.setDebugMode(isDebug)
    }
    
    /**
     * Get the current SDK configuration
     */
    fun getConfiguration(): SdkConfiguration = router.getConfiguration()
    
    /**
     * Update the SDK configuration
     */
    fun updateConfiguration(config: SdkConfiguration) {
        router.updateConfiguration(config)
    }
    
    /**
     * Update the SDK configuration using a builder lambda
     */
    fun updateConfiguration(block: SdkConfiguration.() -> SdkConfiguration) {
        router.updateConfiguration(block)
    }
    
    /**
     * Get list of all available providers
     */
    fun getAvailableProviders(): List<Provider> {
        return router.getAvailableProviders()
    }

    fun getProvider(providerName: String): Provider {
        return router.getProvider(providerName)
    }

    /**
     * Perform a standard (non-streaming) chat completion
     */
    suspend fun chat(request: ChatRequest): ChatResponse {
        return router.chat(request)
    }
    
    /**
     * Perform a streaming chat completion
     */
    fun chatStream(request: ChatRequest): Flow<StreamEvent> {
        return router.chatStream(request)
    }
}
