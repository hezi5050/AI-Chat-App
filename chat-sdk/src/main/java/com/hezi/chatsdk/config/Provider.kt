package com.hezi.chatsdk.config

enum class Provider {
    OPENAI,
    MOCK;

    fun key(): String = name.lowercase()
}

