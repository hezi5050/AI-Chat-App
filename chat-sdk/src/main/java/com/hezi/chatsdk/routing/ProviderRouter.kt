package com.hezi.chatsdk.routing

import com.hezi.chatsdk.config.Provider
import com.hezi.chatsdk.config.SdkConfiguration
import com.hezi.chatsdk.models.ChatRequest
import com.hezi.chatsdk.models.ChatResponse
import com.hezi.chatsdk.models.StreamEvent
import com.hezi.chatsdk.providers.LlmProvider
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.system.measureTimeMillis

/**
 * Routes chat requests to the appropriate LLM provider based on configuration.
 * Providers are injected via Hilt's multibinding.
 */
@Singleton
class ProviderRouter @Inject constructor(
    private val providers: Map<Provider, @JvmSuppressWildcards LlmProvider>
) {
    
    @Volatile
    private var currentConfiguration: SdkConfiguration = SdkConfiguration()
    
    /**
     * Returns the current configuration.
     */
    fun getConfiguration(): SdkConfiguration = currentConfiguration
    
    /**
     * Updates the current configuration.
     */
    fun updateConfiguration(config: SdkConfiguration) {
        currentConfiguration = config
    }
    
    /**
     * Executes a chat request using the configured provider.
     * Measures and includes request latency in the response.
     */
    suspend fun chat(request: ChatRequest): ChatResponse {
        val config = currentConfiguration
        val provider = getProvider(config.provider)
        
        var response: ChatResponse? = null
        val latency = measureTimeMillis {
            response = provider.chat(request)
        }
        
        // Update response with actual latency if not already set
        return response!!.copy(latencyMs = latency)
    }
    
    /**
     * Executes a streaming chat request using the configured provider.
     */
    fun chatStream(request: ChatRequest): Flow<StreamEvent> {
        val config = currentConfiguration
        val provider = getProvider(config.provider)
        return provider.chatStream(request)
    }
    
    /**
     * Retrieves the provider by type or throws an exception if not found.
     */
    private fun getProvider(providerType: Provider): LlmProvider {
        return providers[providerType]
            ?: throw IllegalArgumentException("Provider '$providerType' not registered. Available providers: ${providers.keys}")
    }
    
    /**
     * Returns the list of available providers.
     */
    fun getAvailableProviders(): List<Provider> = providers.keys.toList()
}

