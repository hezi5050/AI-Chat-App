package com.hezi.chatsdk

import com.hezi.chatsdk.config.Provider
import com.hezi.chatsdk.config.SdkConfiguration
import com.hezi.chatsdk.models.ChatRequest
import com.hezi.chatsdk.models.ChatResponse
import com.hezi.chatsdk.models.StreamEvent
import com.hezi.chatsdk.routing.ProviderRouter
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Main SDK interface for AI chat functionality.
 * Provides methods to send chat requests and manage configuration at runtime.
 */
@Singleton
class AiChatSdk @Inject constructor(
    private val router: ProviderRouter
) {
    
    /**
     * Returns the current SDK configuration.
     */
    fun getConfiguration(): SdkConfiguration = router.getConfiguration()
    
    /**
     * Updates the SDK configuration at runtime.
     * Changes take effect immediately for subsequent requests.
     */
    fun updateConfiguration(config: SdkConfiguration) {
        router.updateConfiguration(config)
    }
    
    /**
     * Updates specific configuration parameters without replacing the entire config.
     */
    fun updateConfiguration(block: SdkConfiguration.() -> SdkConfiguration) {
        val newConfig = block(getConfiguration())
        router.updateConfiguration(newConfig)
    }
    
    /**
     * Sends a standard (non-streaming) chat request.
     */
    suspend fun chat(request: ChatRequest): ChatResponse {
        return router.chat(request)
    }
    
    /**
     * Sends a streaming chat request.
     * Returns a Flow that emits tokens as they are received.
     */
    fun chatStream(request: ChatRequest): Flow<StreamEvent> {
        return router.chatStream(request)
    }
    
    /**
     * Returns the list of available providers.
     */
    fun getAvailableProviders(): List<Provider> {
        return router.getAvailableProviders()
    }
}

