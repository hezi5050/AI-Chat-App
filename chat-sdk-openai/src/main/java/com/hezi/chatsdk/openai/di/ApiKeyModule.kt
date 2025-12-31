package com.hezi.chatsdk.openai.di

import com.hezi.chatsdk.openai.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ApiKeyModule {

    @Provides
    @Singleton
    @OpenAiApiKey
    fun provideOpenAiApiKey(): String {
        return BuildConfig.OPENAI_API_KEY
    }
}