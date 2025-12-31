package com.hezi.chatsdk.openai.di

import javax.inject.Qualifier

/**
 * Qualifier for OpenAI API key injection.
 * The app module must provide this value from BuildConfig.
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class OpenAiApiKey