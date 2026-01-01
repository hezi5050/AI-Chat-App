package com.hezi.chatsdk.mock

import com.hezi.chatsdk.core.config.Provider
import com.hezi.chatsdk.core.config.SdkConfiguration
import com.hezi.chatsdk.core.models.ChatRequest
import com.hezi.chatsdk.core.models.ChatResponse
import com.hezi.chatsdk.core.models.StreamEvent
import com.hezi.chatsdk.core.models.TokenUsage
import com.hezi.chatsdk.core.providers.LlmProvider
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.random.Random

class MockProvider(
    private val providerInfo: Provider
) : LlmProvider {
    
    override fun getProvider(): Provider = providerInfo
    
    override suspend fun chat(request: ChatRequest, config: SdkConfiguration): ChatResponse {
        // Simulate network delay
        delay(Random.nextLong(500, 1500))
        
        val mockResponse = generateMockResponse(request)
        
        return ChatResponse(
            text = mockResponse,
            provider = providerInfo,
            model = config.model,
            latencyMs = Random.nextLong(500, 1500),
            tokenUsage = TokenUsage(
                promptTokens = request.messages.sumOf { it.content.length / 4 },
                completionTokens = mockResponse.length / 4,
                totalTokens = (request.messages.sumOf { it.content.length } + mockResponse.length) / 4
            )
        )
    }
    
    override fun chatStream(request: ChatRequest, config: SdkConfiguration): Flow<StreamEvent> = flow {
        val startTime = System.currentTimeMillis()
        val mockResponse = generateMockResponse(request)
        
        // Stream the response word by word
        val words = mockResponse.split(" ")
        for (word in words) {
            delay(Random.nextLong(50, 150))
            emit(StreamEvent.Delta("$word "))
        }
        
        val latency = System.currentTimeMillis() - startTime
        emit(
            StreamEvent.Complete(
                ChatResponse(
                    text = mockResponse,
                    provider = providerInfo,
                    model = config.model,
                    latencyMs = latency,
                    tokenUsage = TokenUsage(
                        promptTokens = request.messages.sumOf { it.content.length / 4 },
                        completionTokens = mockResponse.length / 4,
                        totalTokens = (request.messages.sumOf { it.content.length } + mockResponse.length) / 4
                    )
                )
            )
        )
    }
    
    private fun generateMockResponse(request: ChatRequest): String {
        val lastMessage = request.messages.lastOrNull()?.content ?: ""
        
        return when {
            lastMessage.contains("hello", ignoreCase = true) -> 
                "Hello! I'm a mock AI assistant. How can I help you today?"
            
            lastMessage.contains("weather", ignoreCase = true) -> 
                "I'm a mock provider, so I don't have real weather data. But let's pretend it's sunny and 72°F!"
            
            lastMessage.contains("test", ignoreCase = true) -> 
                "This is a test response from the mock provider. All systems are working correctly!"
            
            lastMessage.length > 100 -> 
                "That's quite a long message! In a real scenario, I would provide a detailed response. For now, here's a mock reply."
            
            else -> 
                "This is a mock response to your message: \"$lastMessage\". The mock provider simulates AI responses for testing purposes."
        }
    }
}
