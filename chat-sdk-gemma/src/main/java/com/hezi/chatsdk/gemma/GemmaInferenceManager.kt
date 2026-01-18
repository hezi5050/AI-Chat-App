package com.hezi.chatsdk.gemma

import android.content.Context
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Manages MediaPipe LLM Inference lifecycle for Gemma model.
 * Handles model loading, inference execution, and resource cleanup.
 */
class GemmaInferenceManager(
    private val context: Context,
    private val modelPath: String
) {
    private var llmInference: LlmInference? = null
    private var currentMaxTokens: Int = 0
    private val lock = Any()
    
    /**
     * Checks if the model file exists at the configured path.
     */
    fun isModelAvailable(): Boolean {
        return File(modelPath).exists()
    }
    
    /**
     * Initializes the LLM inference engine with the Gemma model.
     * Must be called before performing any inference.
     * 
     * @param maxTokens Maximum tokens for input + output combined (from SdkConfiguration)
     * @throws IllegalStateException if the model file doesn't exist
     * @throws Exception if model loading fails
     */
    suspend fun initialize(maxTokens: Int) = withContext(Dispatchers.IO) {
        synchronized(lock) {
            // Reinitialize if maxTokens changed
            if (llmInference != null && currentMaxTokens == maxTokens) {
                return@withContext
            }
            
            // Close existing instance if maxTokens changed
            if (llmInference != null && currentMaxTokens != maxTokens) {
                llmInference?.close()
                llmInference = null
            }
            
            if (!isModelAvailable()) {
                throw IllegalStateException(
                    "Gemma model not found at: $modelPath. " +
                    "Download .task model from Kaggle and push using: adb push gemma3-1b-it-int4.task /data/local/tmp/llm/"
                )
            }
            
            // Use at least 4096 tokens to handle conversation context
            val effectiveMaxTokens = maxOf(maxTokens, 4096)
            
            val options = LlmInference.LlmInferenceOptions.builder()
                .setModelPath(modelPath)
                .setMaxTokens(effectiveMaxTokens)
                .setPreferredBackend(LlmInference.Backend.CPU)
                .build()
            
            llmInference = LlmInference.createFromOptions(context, options)
            currentMaxTokens = effectiveMaxTokens
        }
    }
    
    /**
     * Performs synchronous (non-streaming) text generation.
     * 
     * @param prompt The formatted prompt string
     * @return The complete generated response
     * @throws IllegalStateException if not initialized
     */
    suspend fun generateResponse(prompt: String): String = withContext(Dispatchers.IO) {
        val inference = llmInference 
            ?: throw IllegalStateException("GemmaInferenceManager not initialized. Call initialize() first.")
        
        inference.generateResponse(prompt)
    }
    
    /**
     * Performs streaming text generation with token-by-token results.
     * Uses MediaPipe's async generation with partial result callback.
     * 
     * @param prompt The formatted prompt string
     * @return Flow emitting partial results as they are generated
     */
    fun generateResponseStream(prompt: String): Flow<String> = callbackFlow {
        val inference = llmInference
            ?: throw IllegalStateException("GemmaInferenceManager not initialized. Call initialize() first.")
        
        // MediaPipe LLM Inference uses generateResponseAsync with a listener
        // The listener receives partial results as they are generated
        inference.generateResponseAsync(prompt) { partialResult, done ->
            if (partialResult.isNotEmpty()) {
                trySend(partialResult)
            }
            if (done) {
                close()
            }
        }
        
        awaitClose {
            // Cleanup if needed when flow is cancelled
        }
    }
    
    /**
     * Releases all resources held by the inference engine.
     * Should be called when the provider is no longer needed.
     */
    fun close() {
        synchronized(lock) {
            llmInference?.close()
            llmInference = null
        }
    }
}
