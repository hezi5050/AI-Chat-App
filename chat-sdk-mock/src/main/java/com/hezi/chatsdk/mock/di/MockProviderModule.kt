package com.hezi.chatsdk.mock.di

import com.hezi.chatsdk.core.config.Provider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier
import javax.inject.Singleton

const val MOCK_PROVIDER_NAME = "Mock"

@Module
@InstallIn(SingletonComponent::class)
object MockProviderModule {

    @Provides
    @Singleton
    @MockProviderInfo
    fun provideMockProviderInfo(): Provider {
        return Provider(
            name = MOCK_PROVIDER_NAME,
            models = listOf(
                "default"
            )
        )
    }
}

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class MockProviderInfo

