package com.hezi.chatsdk.gemma.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class GemmaModelPath

@Module
@InstallIn(SingletonComponent::class)
object GemmaModelPathModule {

    @Provides
    @Singleton
    @GemmaModelPath
    fun provideGemmaModelPath(): String {
        // Path to the Gemma 3 1B-IT model in MediaPipe .task format
        // Download from: https://www.kaggle.com/models/google/gemma-3/tfLite
//        return "/data/local/tmp/llm/gemma3-1b-it-int4.task"
        return "/data/local/tmp/llm/gemma3-1b-it-int4.task"
    }
}

