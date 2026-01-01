package com.hezi.chatsdk.di

import com.hezi.chatsdk.core.config.Provider
import com.hezi.chatsdk.core.providers.LlmProvider
import com.hezi.chatsdk.mock.MockProvider
import com.hezi.chatsdk.mock.di.MOCK_PROVIDER_NAME
import com.hezi.chatsdk.mock.di.MockProviderInfo
import com.hezi.chatsdk.openai.OpenAiApiService
import com.hezi.chatsdk.openai.OpenAiProvider
import com.hezi.chatsdk.openai.di.OPEN_AI_PROVIDER_NAME
import com.hezi.chatsdk.openai.di.OpenAiApiKey
import com.hezi.chatsdk.openai.di.OpenAiProviderInfo
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import kotlinx.serialization.json.Json
import javax.inject.Singleton

/**
 * Binds provider implementations into the provider map.
 * Each provider module provides its own Provider metadata.
 */

@Module
@InstallIn(SingletonComponent::class)
object ProviderModule {
    
    @Provides
    @IntoMap
    @ProviderName(MOCK_PROVIDER_NAME)
    @Singleton
    fun provideMockProvider(
        @MockProviderInfo providerInfo: Provider
    ): LlmProvider {
        return MockProvider(providerInfo)
    }

    @Provides
    @IntoMap
    @ProviderName(OPEN_AI_PROVIDER_NAME)
    @Singleton
    fun provideOpenAiProvider(
        apiService: OpenAiApiService,
        json: Json,
        @OpenAiApiKey apiKey: String,
        @OpenAiProviderInfo providerInfo: Provider
    ): LlmProvider {
        return OpenAiProvider(
            apiService = apiService,
            apiKey = apiKey,
            json = json,
            providerInfo = providerInfo
        )
    }
}

