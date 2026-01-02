package com.hezi.aichatapp.ui.chat

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hezi.aichatapp.R
import com.hezi.aichatapp.commands.CommandHandler
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
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()
    
    private val _currentProvider = MutableStateFlow<Provider?>(null)
    val currentProvider: StateFlow<Provider?> = _currentProvider.asStateFlow()

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
                        // Clear command - only affects conversation history, not configuration
                        addCommandMessages(text, context.getString(R.string.conversation_cleared))
                        clearConversation()
                    }
                }
            }
        }
    }

    private fun addCommandMessages(commandText: String, resultText: String) {
        // Add command message to UI
        val commandMessage = UiMessage(
            id = UUID.randomUUID().toString(),
            role = MessageRole.USER,
            content = commandText
        )
        
        // Add result message
        val resultMessage = UiMessage(
            id = UUID.randomUUID().toString(),
            role = MessageRole.SYSTEM,
            content = resultText
        )
        
        _uiState.update {
            it.copy(
                messages = it.messages + commandMessage + resultMessage,
                inputText = ""
            )
        }
    }

    private fun sendAiMessage(text: String) {
        // Add user message to UI
        val userMessage = UiMessage(
            id = UUID.randomUUID().toString(),
            role = MessageRole.USER,
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

        // Add to conversation history
        conversationHistory.add(
            ChatMessage(role = MessageRole.USER, content = text)
        )

        // Send to SDK with streaming
        viewModelScope.launch {
            try {
                val request = ChatRequest(messages = conversationHistory.toList())
                val assistantMessageId = UUID.randomUUID().toString()
                val streamingMessage = UiMessage(
                    id = assistantMessageId,
                    role = MessageRole.ASSISTANT,
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
                            // Finalize the message
                            _uiState.update { state ->
                                val updatedMessages = state.messages.map { msg ->
                                    if (msg.id == assistantMessageId) {
                                        msg.copy(
                                            content = event.response.text,
                                            isStreaming = false
                                        )
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

                            // Add to conversation history
                            conversationHistory.add(
                                ChatMessage(
                                    role = MessageRole.ASSISTANT,
                                    content = event.response.text
                                )
                            )
                        }

                            is StreamEvent.Error -> {
                                // Remove the streaming message and show error
                                _uiState.update { state ->
                                    state.copy(
                                        messages = state.messages.filter { it.id != assistantMessageId },
                                        isLoading = false,
                                        isStreaming = false,
                                        error = context.getString(
                                            R.string.error_format,
                                            event.error.message ?: context.getString(R.string.error_unknown)
                                        )
                                    )
                                }
                            }
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isStreaming = false,
                        error = context.getString(R.string.error_send_message, e.message ?: context.getString(R.string.error_unknown))
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

