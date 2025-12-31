package com.hezi.chatsdk.di

import com.hezi.chatsdk.core.config.Provider
import com.hezi.chatsdk.openai.di.OpenAiApiKey
import com.hezi.chatsdk.di.ProviderKey
import com.hezi.chatsdk.core.providers.LlmProvider
import com.hezi.chatsdk.mock.MockProvider
import com.hezi.chatsdk.openai.OpenAiProvider
import com.hezi.chatsdk.openai.OpenAiApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import kotlinx.serialization.json.Json
import javax.inject.Singleton

/**
 * Provides the Mock provider implementation.
 * Other providers bind themselves in their respective modules.
 */
@Module
@InstallIn(SingletonComponent::class)
object ProviderModule {
    
    @Provides
    @IntoMap
    @ProviderKey(Provider.MOCK)
    @Singleton
    fun provideMockProvider(): LlmProvider {
        return MockProvider()
    }

    @Provides
    @IntoMap
    @ProviderKey(Provider.OPENAI)
    @Singleton
    fun provideOpenAiProvider(
        apiService: OpenAiApiService,
        json: Json,
        @OpenAiApiKey apiKey: String
    ): LlmProvider {
        return OpenAiProvider(
            apiService = apiService,
            apiKey = apiKey,
            json = json
        )
    }
}

