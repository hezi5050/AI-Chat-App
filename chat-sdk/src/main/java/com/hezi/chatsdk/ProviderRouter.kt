package com.hezi.chatsdk

import com.hezi.chatsdk.core.config.Provider
import com.hezi.chatsdk.core.config.SdkConfiguration
import com.hezi.chatsdk.core.models.ChatRequest
import com.hezi.chatsdk.core.models.ChatResponse
import com.hezi.chatsdk.core.models.StreamEvent
import com.hezi.chatsdk.core.providers.LlmProvider
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.system.measureTimeMillis

@Singleton
class ProviderRouter @Inject constructor(
    private val providers: Map<Provider, @JvmSuppressWildcards LlmProvider>
) {
    @Volatile
    private var currentConfiguration: SdkConfiguration = SdkConfiguration()

    fun updateConfiguration(config: SdkConfiguration) {
        currentConfiguration = config
    }

    fun updateConfiguration(block: SdkConfiguration.() -> SdkConfiguration) {
        currentConfiguration = currentConfiguration.block()
    }

    fun getConfiguration(): SdkConfiguration = currentConfiguration

    fun getAvailableProviders(): List<Provider> {
        return providers.keys.toList()
    }

    suspend fun chat(request: ChatRequest): ChatResponse {
        val provider = getProvider(currentConfiguration.provider)
        var response: ChatResponse
        val latency = measureTimeMillis {
            response = provider.chat(request, currentConfiguration)
        }
        return response.copy(latencyMs = latency)
    }

    fun chatStream(request: ChatRequest): Flow<StreamEvent> {
        val provider = getProvider(currentConfiguration.provider)
        return provider.chatStream(request, currentConfiguration)
    }

    private fun getProvider(providerType: Provider): LlmProvider {
        return providers[providerType] 
            ?: throw IllegalArgumentException("Provider $providerType not registered")
    }
}

