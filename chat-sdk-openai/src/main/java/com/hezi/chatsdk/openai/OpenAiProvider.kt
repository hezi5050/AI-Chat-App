package com.hezi.chatsdk.openai

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
import kotlinx.serialization.json.Json

class OpenAiProvider(
    private val apiService: OpenAiApiService,
    private val apiKey: String,
    private val json: Json
) : LlmProvider {
    
    override fun getProvider(): Provider = Provider.OPENAI
    
    override suspend fun chat(request: ChatRequest, config: SdkConfiguration): ChatResponse = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        
        val openAiRequest = OpenAiChatRequest(
            model = config.model,
            messages = request.messages.map { it.toOpenAiMessage() },
            temperature = config.temperature,
            maxTokens = config.maxTokens,
            stream = false
        )
        
        val response = apiService.chatCompletion(
            authorization = "Bearer $apiKey",
            request = openAiRequest
        )
        
        if (!response.isSuccessful) {
            throw Exception("OpenAI API error: ${response.code()} ${response.message()}")
        }
        
        val body = response.body() ?: throw Exception("Empty response from OpenAI")
        val latency = System.currentTimeMillis() - startTime
        
        ChatResponse(
            text = body.choices.firstOrNull()?.message?.content ?: "",
            provider = Provider.OPENAI,
            model = body.model,
            latencyMs = latency,
            tokenUsage = body.usage?.let {
                TokenUsage(
                    promptTokens = it.promptTokens,
                    completionTokens = it.completionTokens,
                    totalTokens = it.totalTokens
                )
            }
        )
    }
    
    override fun chatStream(request: ChatRequest, config: SdkConfiguration): Flow<StreamEvent> = flow {
        val startTime = System.currentTimeMillis()
        val accumulatedText = StringBuilder()
        
        val openAiRequest = OpenAiChatRequest(
            model = config.model,
            messages = request.messages.map { it.toOpenAiMessage() },
            temperature = config.temperature,
            maxTokens = config.maxTokens,
            stream = true
        )
        
        try {
            val response = apiService.chatCompletionStream(
                authorization = "Bearer $apiKey",
                request = openAiRequest
            )
            
            if (!response.isSuccessful) {
                emit(StreamEvent.Error(Exception("OpenAI API error: ${response.code()}")))
                return@flow
            }
            
            val body = response.body() ?: run {
                emit(StreamEvent.Error(Exception("Empty response from OpenAI")))
                return@flow
            }
            
            body.source().use { source ->
                val buffer = okio.Buffer()
                while (!source.exhausted()) {
                    source.read(buffer, 8192)
                    val chunk = buffer.readUtf8()
                    
                    chunk.lines().forEach { line ->
                        if (line.startsWith("data: ")) {
                            val data = line.substring(6).trim()
                            if (data == "[DONE]") return@forEach
                            
                            try {
                                val streamResponse = json.decodeFromString<OpenAiStreamResponse>(data)
                                val content = streamResponse.choices.firstOrNull()?.delta?.content
                                
                                if (content != null) {
                                    accumulatedText.append(content)
                                    emit(StreamEvent.Delta(content))
                                }
                            } catch (e: Exception) {
                                // Ignore parsing errors for SSE
                            }
                        }
                    }
                }
            }
            
            val latency = System.currentTimeMillis() - startTime
            emit(
                StreamEvent.Complete(
                    ChatResponse(
                        text = accumulatedText.toString(),
                        provider = Provider.OPENAI,
                        model = config.model,
                        latencyMs = latency,
                        tokenUsage = null
                    )
                )
            )
        } catch (e: Exception) {
            emit(StreamEvent.Error(e))
        }
    }.flowOn(Dispatchers.IO)
    
    private fun ChatMessage.toOpenAiMessage(): OpenAiMessage {
        return OpenAiMessage(
            role = when (role) {
                MessageRole.USER -> "user"
                MessageRole.ASSISTANT -> "assistant"
                MessageRole.SYSTEM -> "system"
            },
            content = content
        )
    }
}
