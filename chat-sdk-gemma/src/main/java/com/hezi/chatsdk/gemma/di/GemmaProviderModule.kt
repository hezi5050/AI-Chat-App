package com.hezi.chatsdk.gemma.di

import com.hezi.chatsdk.core.config.Provider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier
import javax.inject.Singleton

const val GEMMA_PROVIDER_NAME = "Gemma"

@Module
@InstallIn(SingletonComponent::class)
object GemmaProviderModule {

    @Provides
    @Singleton
    @GemmaProviderInfo
    fun provideGemmaProviderInfo(): Provider {
        return Provider(
            name = GEMMA_PROVIDER_NAME,
            models = listOf(
                "gemma-3-1b-it"
            )
        )
    }
}

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class GemmaProviderInfo

