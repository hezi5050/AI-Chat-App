package com.hezi.chatsdk.provider_router

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

/**
 * Default implementation of ProviderRouter that routes chat requests to the appropriate LLM provider.
 */
@Singleton
class AiChatProviderRouter @Inject constructor(
    private val providers: Map<String, @JvmSuppressWildcards LlmProvider>
) : ProviderRouter {

    @Volatile
    private var currentConfiguration: SdkConfiguration = SdkConfiguration()

    override fun updateConfiguration(config: SdkConfiguration) {
        currentConfiguration = config
    }

    override fun updateConfiguration(block: SdkConfiguration.() -> SdkConfiguration) {
        currentConfiguration = currentConfiguration.block()
    }

    override fun getConfiguration(): SdkConfiguration = currentConfiguration

    override fun getAvailableProviders(): List<Provider> {
        return providers.values.map { it.getProvider() }
    }

    override suspend fun chat(request: ChatRequest): ChatResponse {
        val provider = getLlmProvider(currentConfiguration.providerName)
        var response: ChatResponse
        val latency = measureTimeMillis {
            response = provider.chat(request, currentConfiguration)
        }
        return response.copy(latencyMs = latency)
    }

    override fun chatStream(request: ChatRequest): Flow<StreamEvent> {
        val provider = getLlmProvider(currentConfiguration.providerName)
        return provider.chatStream(request, currentConfiguration)
    }

    override fun getProvider(providerName: String): Provider {
        return providers[providerName]?.getProvider()
            ?: throw IllegalArgumentException("Provider $providerName not registered. Available: ${providers.keys}")
    }

    private fun getLlmProvider(providerId: String): LlmProvider {
        return providers[providerId]
            ?: throw IllegalArgumentException("Provider $providerId not registered. Available: ${providers.keys}")
    }
}