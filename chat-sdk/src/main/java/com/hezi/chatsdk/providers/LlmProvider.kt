package com.hezi.chatsdk.providers

import com.hezi.chatsdk.config.Provider
import com.hezi.chatsdk.models.ChatRequest
import com.hezi.chatsdk.models.ChatResponse
import com.hezi.chatsdk.models.StreamEvent
import kotlinx.coroutines.flow.Flow

/**
 * Base interface for all LLM providers.
 * Each provider must implement both standard and streaming chat methods.
 */
interface LlmProvider {
    
    /**
     * Returns the provider type
     */
    fun getProvider(): Provider
    
    /**
     * Performs a standard (non-streaming) chat completion request.
     * 
     * @param request The chat request containing messages and parameters
     * @return The complete chat response with latency and token usage
     * @throws Exception if the request fails
     */
    suspend fun chat(request: ChatRequest): ChatResponse
    
    /**
     * Performs a streaming chat completion request.
     * 
     * @param request The chat request containing messages and parameters
     * @return A Flow emitting StreamEvent.Delta for each token, followed by StreamEvent.Complete
     */
    fun chatStream(request: ChatRequest): Flow<StreamEvent>
}

