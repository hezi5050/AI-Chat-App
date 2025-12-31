package com.hezi.chatsdk.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SdkModule {
    
    @Provides
    @Singleton
    fun provideSdkConfig(): SdkConfig {
        return SdkConfig()
    }
}

