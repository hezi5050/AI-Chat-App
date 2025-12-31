package com.hezi.chatsdk.core.config

enum class Provider {
    OPENAI,
    MOCK;

    fun key(): String = name.lowercase()
}

