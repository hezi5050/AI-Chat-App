package com.hezi.chatsdk.gemma

import android.content.Context
import com.hezi.chatsdk.core.config.Provider
import com.hezi.chatsdk.core.config.SdkConfiguration
import com.hezi.chatsdk.core.models.ChatMessage
import com.hezi.chatsdk.core.models.ChatRequest
import com.hezi.chatsdk.core.models.ChatResponse
import com.hezi.chatsdk.core.models.MessageRole
import com.hezi.chatsdk.core.models.StreamEvent
import com.hezi.chatsdk.core.models.TokenUsage
import com.hezi.chatsdk.core.providers.LlmProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

/**
 * LlmProvider implementation for Google's Gemma model using MediaPipe LLM Inference.
 * Runs inference on-device without requiring network access.
 */
class GemmaProvider(
    private val context: Context,
    private val providerInfo: Provider,
    private val modelPath: String
) : LlmProvider {
    
    private val inferenceManager: GemmaInferenceManager by lazy {
        GemmaInferenceManager(context, modelPath)
    }
    
    override fun getProvider(): Provider = providerInfo
    
    override suspend fun chat(request: ChatRequest, config: SdkConfiguration): ChatResponse = 
        withContext(Dispatchers.IO) {
            ensureInitialized(config.maxTokens)
            
            val startTime = System.currentTimeMillis()
            val prompt = formatPrompt(request.messages, config)
            
            val response = inferenceManager.generateResponse(prompt)
            val latency = System.currentTimeMillis() - startTime
            
            ChatResponse(
                text = response.trim(),
                provider = providerInfo,
                model = config.model,
                latencyMs = latency,
                tokenUsage = estimateTokenUsage(prompt, response)
            )
        }
    
    override fun chatStream(request: ChatRequest, config: SdkConfiguration): Flow<StreamEvent> = flow {
        ensureInitialized(config.maxTokens)
        
        val startTime = System.currentTimeMillis()
        val prompt = formatPrompt(request.messages, config)
        val accumulatedText = StringBuilder()
        
        try {
            inferenceManager.generateResponseStream(prompt).collect { partialResult ->
                accumulatedText.append(partialResult)
                emit(StreamEvent.Delta(partialResult))
            }
            
            val latency = System.currentTimeMillis() - startTime
            val fullResponse = accumulatedText.toString().trim()
            
            emit(
                StreamEvent.Complete(
                    ChatResponse(
                        text = fullResponse,
                        provider = providerInfo,
                        model = config.model,
                        latencyMs = latency,
                        tokenUsage = estimateTokenUsage(prompt, fullResponse)
                    )
                )
            )
        } catch (e: Exception) {
            emit(StreamEvent.Error(e))
        }
    }.flowOn(Dispatchers.IO)
    
    /**
     * Ensures the inference engine is initialized with the configured maxTokens.
     * Will reinitialize if maxTokens changed.
     */
    private suspend fun ensureInitialized(maxTokens: Int) {
        inferenceManager.initialize(maxTokens)
    }
    
    /**
     * Formats messages into Gemma's chat template format.
     * 
     * Gemma uses the following format:
     * <start_of_turn>user
     * {user message}<end_of_turn>
     * <start_of_turn>model
     * {assistant message}<end_of_turn>
     * <start_of_turn>model
     */
    private fun formatPrompt(messages: List<ChatMessage>, config: SdkConfiguration): String {
        val builder = StringBuilder()
        
        for (message in messages) {
            when (message.role) {
                MessageRole.SYSTEM -> {
                    // Gemma 3 supports system instructions
                    builder.append("<start_of_turn>user\n")
                    builder.append("System instruction: ${message.content}")
                    builder.append("<end_of_turn>\n")
                }
                MessageRole.USER -> {
                    builder.append("<start_of_turn>user\n")
                    builder.append(message.content)
                    builder.append("<end_of_turn>\n")
                }
                MessageRole.ASSISTANT -> {
                    builder.append("<start_of_turn>model\n")
                    builder.append(message.content)
                    builder.append("<end_of_turn>\n")
                }
            }
        }
        
        // Add the model turn indicator to prompt the model to respond
        builder.append("<start_of_turn>model\n")
        
        return builder.toString()
    }
    
    /**
     * Estimates token usage based on character count.
     * This is an approximation since we don't have access to the actual tokenizer.
     * Rough estimate: ~4 characters per token for English text.
     */
    private fun estimateTokenUsage(prompt: String, response: String): TokenUsage {
        val promptTokens = prompt.length / 4
        val completionTokens = response.length / 4
        return TokenUsage(
            promptTokens = promptTokens,
            completionTokens = completionTokens,
            totalTokens = promptTokens + completionTokens
        )
    }
    
    /**
     * Checks if the model file is available.
     */
    fun isModelAvailable(): Boolean = inferenceManager.isModelAvailable()
}

