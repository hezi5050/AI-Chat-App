package com.hezi.aichatapp.ui.chat

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hezi.aichatapp.R
import com.hezi.aichatapp.commands.CommandHandler
import com.hezi.aichatapp.data.DiagnosticsRepository
import com.hezi.aichatapp.data.repository.ConversationRepository
import com.hezi.chatsdk.AiChatSdk
import com.hezi.chatsdk.core.config.Provider
import com.hezi.chatsdk.core.models.ChatMessage
import com.hezi.chatsdk.core.models.ChatRequest
import com.hezi.chatsdk.core.models.MessageRole
import com.hezi.chatsdk.core.models.StreamEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val sdk: AiChatSdk,
    private val commandHandler: CommandHandler,
    private val diagnosticsRepository: DiagnosticsRepository,
    private val conversationRepository: ConversationRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private val _currentProvider = MutableStateFlow<Provider?>(null)
    val currentProvider: StateFlow<Provider?> = _currentProvider.asStateFlow()

    private var currentConversationId: Long? = null

    private val conversationHistory = mutableListOf<ChatMessage>()

    init {
        // Get initial provider from SDK configuration
        val config = sdk.getConfiguration()
        _currentProvider.value = sdk.getProvider(config.providerName)
    }

    fun getCurrentModel(): String {
        return sdk.getConfiguration().model
    }

    fun refreshProviderState() {
        val config = sdk.getConfiguration()
        _currentProvider.value = sdk.getProvider(config.providerName)
    }

    suspend fun createNewConversation(firstMessage: String? = null): Long {
        val config = sdk.getConfiguration()
        // Use first message as title, truncated to 50 chars, or default title
        val title = firstMessage?.take(50) ?: "New Conversation"
        val conversationId = conversationRepository.createConversation(
            title = title,
            providerName = config.providerName,
            model = config.model
        )
        currentConversationId = conversationId
        return conversationId
    }

    fun loadConversation(conversationId: Long) {
        viewModelScope.launch {
            val conversationWithMessages = conversationRepository.getConversationWithMessages(conversationId)

            if (conversationWithMessages != null) {
                currentConversationId = conversationId

                // Clear current state
                conversationHistory.clear()

                // Load messages into UI
                _uiState.update {
                    it.copy(messages = conversationWithMessages.messages)
                }

                // Rebuild conversation history for SDK (only user and assistant messages)
                conversationWithMessages.messages.forEach { uiMessage ->
                    when (uiMessage.type) {
                        UiMessageType.USER -> {
                            conversationHistory.add(
                                ChatMessage(role = MessageRole.USER, content = uiMessage.content)
                            )
                        }

                        UiMessageType.ASSISTANT -> {
                            conversationHistory.add(
                                ChatMessage(role = MessageRole.ASSISTANT, content = uiMessage.content)
                            )
                        }

                        UiMessageType.SYSTEM -> {
                            // System messages are not added to SDK conversation history
                        }
                    }
                }
            }
        }
    }

    fun startNewConversation() {
        // Clear UI and conversation history, but don't create DB entry until first message
        clearConversation()
        currentConversationId = null
    }

    private fun deleteCurrentConversation() {
        viewModelScope.launch {
            currentConversationId?.let { convId ->
                conversationRepository.deleteConversation(convId)
                currentConversationId = null
            }
        }
    }

    fun updateInputText(text: String) {
        _uiState.update { it.copy(inputText = text) }
    }

    fun sendMessage() {
        val text = _uiState.value.inputText.trim()
        if (text.isBlank() || _uiState.value.isLoading) return

        // Try to handle as a command
        when (val result = commandHandler.handle(text)) {
            is CommandHandler.CommandResult.NotHandled -> {
                // Not a command, send as regular message to AI
                sendAiMessage(text)
            }

            is CommandHandler.CommandResult.Handled -> {
                // Command was handled, process the result
                when (result) {
                    is CommandHandler.CommandResult.Handled.Message -> {
                        // Show command and result message
                        addCommandMessages(text, result.text)
                        // Refresh provider state in case configuration changed (e.g., /model, /temp)
                        refreshProviderState()
                    }

                    is CommandHandler.CommandResult.Handled.ClearHistory -> {
                        // Clear command - delete conversation from database and clear UI
                        addCommandMessages(text, context.getString(R.string.conversation_cleared))
                        deleteCurrentConversation()
                        clearConversation()
                    }
                }
            }
        }
    }

    private fun addCommandMessages(commandText: String, resultText: String) {
        // Add command message to UI (commands are NOT saved to database)
        val commandMessage = UiMessage(
            id = UUID.randomUUID().toString(),
            type = UiMessageType.USER,
            content = commandText
        )

        // Add result message
        val resultMessage = UiMessage(
            id = UUID.randomUUID().toString(),
            type = UiMessageType.SYSTEM,
            content = resultText
        )

        _uiState.update {
            it.copy(
                messages = it.messages + commandMessage + resultMessage,
                inputText = ""
            )
        }

        // Commands are not persisted to database - they are transient UI messages only
    }

    private fun sendAiMessage(text: String) {
        // Add user message to UI
        val userMessage = UiMessage(
            id = UUID.randomUUID().toString(),
            type = UiMessageType.USER,
            content = text
        )
        _uiState.update {
            it.copy(
                messages = it.messages + userMessage,
                inputText = "",
                isLoading = true,
                isStreaming = true
            )
        }

        // Save user message to database (create conversation if needed)
        viewModelScope.launch {
            var convId = currentConversationId

            // Create conversation if it doesn't exist yet (use first message as title)
            if (convId == null) {
                convId = createNewConversation(firstMessage = text)
            }

            try {
                conversationRepository.saveMessage(convId, userMessage)
            } catch (e: Exception) {
                createNewConversation(firstMessage = text)
                currentConversationId?.let { newConvId ->
                    conversationRepository.saveMessage(newConvId, userMessage)
                }
            }
        }

        // Add to conversation history (SDK format)
        conversationHistory.add(
            ChatMessage(role = MessageRole.USER, content = text)
        )

        // Send to SDK with streaming
        viewModelScope.launch {
            val config = sdk.getConfiguration()
            val provider = sdk.getProvider(config.providerName)
            val streamStartTime = System.currentTimeMillis()

            try {
                val request = ChatRequest(messages = conversationHistory.toList())
                val assistantMessageId = UUID.randomUUID().toString()
                val streamingMessage = UiMessage(
                    id = assistantMessageId,
                    type = UiMessageType.ASSISTANT,
                    content = "",
                    isStreaming = true
                )

                // Add empty assistant message for streaming
                _uiState.update {
                    it.copy(messages = it.messages + streamingMessage)
                }

                val accumulatedText = StringBuilder()

                sdk.chatStream(request).collect { event ->
                    when (event) {
                        is StreamEvent.Delta -> {
                            accumulatedText.append(event.text)
                            _uiState.update { state ->
                                val updatedMessages = state.messages.map { msg ->
                                    if (msg.id == assistantMessageId) {
                                        msg.copy(content = accumulatedText.toString())
                                    } else {
                                        msg
                                    }
                                }
                                state.copy(messages = updatedMessages)
                            }
                        }

                        is StreamEvent.Complete -> {
                            val latency = System.currentTimeMillis() - streamStartTime

                            // Record success in diagnostics
                            diagnosticsRepository.recordSuccess(
                                provider = provider.name,
                                model = config.model,
                                latencyMs = latency,
                                tokenUsage = event.response.tokenUsage
                            )

                            // Finalize the message
                            val finalMessage = UiMessage(
                                id = assistantMessageId,
                                type = UiMessageType.ASSISTANT,
                                content = event.response.text,
                                isStreaming = false
                            )

                            _uiState.update { state ->
                                val updatedMessages = state.messages.map { msg ->
                                    if (msg.id == assistantMessageId) {
                                        finalMessage
                                    } else {
                                        msg
                                    }
                                }
                                state.copy(
                                    messages = updatedMessages,
                                    isLoading = false,
                                    isStreaming = false
                                )
                            }

                            // Save assistant message to database (ensure conversation exists)
                            viewModelScope.launch {
                                val convId = currentConversationId
                                if (convId != null) {
                                    try {
                                        conversationRepository.saveMessage(convId, finalMessage)
                                    } catch (e: Exception) {
                                        // If save fails, log but don't crash (message is already in UI)
                                    }
                                }
                            }

                            // Add to conversation history
                            conversationHistory.add(
                                ChatMessage(
                                    role = MessageRole.ASSISTANT,
                                    content = event.response.text
                                )
                            )
                        }

                        is StreamEvent.Error -> {
                            // Record error in diagnostics
                            diagnosticsRepository.recordError(
                                provider = provider.name,
                                model = config.model,
                                errorMessage = event.error.message ?: context.getString(R.string.unknown_error)
                            )

                            // Remove the streaming message and show error
                            _uiState.update { state ->
                                state.copy(
                                    messages = state.messages.filter { it.id != assistantMessageId },
                                    isLoading = false,
                                    isStreaming = false,
                                    error = context.getString(
                                        R.string.chat_error_streaming_failed,
                                        event.error.message ?: context.getString(R.string.unknown_error)
                                    )
                                )
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                // Record error in diagnostics
                val config = sdk.getConfiguration()
                val provider = sdk.getProvider(config.providerName)
                diagnosticsRepository.recordError(
                    provider = provider.name,
                    model = config.model,
                    errorMessage = e.message ?: context.getString(R.string.unknown_error)
                )

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isStreaming = false,
                        error = context.getString(
                            R.string.chat_error_send_failed,
                            e.message ?: context.getString(R.string.unknown_error)
                        )
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun clearConversation() {
        conversationHistory.clear()
        _uiState.update { ChatUiState() }
    }
}

