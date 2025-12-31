package com.hezi.chatsdk.core.providers

import com.hezi.chatsdk.core.config.Provider
import com.hezi.chatsdk.core.config.SdkConfiguration
import com.hezi.chatsdk.core.models.ChatRequest
import com.hezi.chatsdk.core.models.ChatResponse
import com.hezi.chatsdk.core.models.StreamEvent
import kotlinx.coroutines.flow.Flow

/**
 * Base interface for all LLM providers.
 * Each provider SDK must implement this interface.
 */
interface LlmProvider {
    
    /**
     * Returns the provider type
     */
    fun getProvider(): Provider
    
    /**
     * Performs a standard (non-streaming) chat completion request.
     * 
     * @param request The chat request containing messages
     * @param config The SDK configuration with model, temperature, etc.
     * @return The complete chat response with latency and token usage
     * @throws Exception if the request fails
     */
    suspend fun chat(request: ChatRequest, config: SdkConfiguration): ChatResponse
    
    /**
     * Performs a streaming chat completion request.
     * 
     * @param request The chat request containing messages
     * @param config The SDK configuration with model, temperature, etc.
     * @return A Flow emitting StreamEvent.Delta for each token, followed by StreamEvent.Complete
     */
    fun chatStream(request: ChatRequest, config: SdkConfiguration): Flow<StreamEvent>
}

