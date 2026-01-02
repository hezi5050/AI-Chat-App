package com.hezi.aichatapp.ui.conversations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hezi.aichatapp.data.repository.Conversation
import com.hezi.aichatapp.data.repository.ConversationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ConversationsViewModel @Inject constructor(
    private val conversationRepository: ConversationRepository
) : ViewModel() {
    
    val conversations: StateFlow<List<Conversation>> = conversationRepository.getAllConversations()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    fun deleteConversation(conversationId: Long) {
        viewModelScope.launch {
            conversationRepository.deleteConversation(conversationId)
        }
    }
}

