package com.hezi.chatsdk.core.models

sealed class StreamEvent {
    data class Delta(val text: String) : StreamEvent()
    data class Complete(val response: ChatResponse) : StreamEvent()
    data class Error(val error: Throwable) : StreamEvent()
}

