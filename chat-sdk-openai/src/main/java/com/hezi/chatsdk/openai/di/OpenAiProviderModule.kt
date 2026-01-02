package com.hezi.chatsdk.openai.di

import com.hezi.chatsdk.core.config.Provider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier
import javax.inject.Singleton

const val OPEN_AI_PROVIDER_NAME = "OpenAI"

@Module
@InstallIn(SingletonComponent::class)
object OpenAiProviderModule {

    @Provides
    @Singleton
    @OpenAiProviderInfo
    fun provideOpenAiProviderInfo(): Provider {
        return Provider(
            name = OPEN_AI_PROVIDER_NAME,
            models = listOf(
                "gpt-4",
                "gpt-4-turbo",
                "gpt-4o",
                "gpt-4o-mini",
                "gpt-3.5-turbo",
                "error-model"
            )
        )
    }
}

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class OpenAiProviderInfo

