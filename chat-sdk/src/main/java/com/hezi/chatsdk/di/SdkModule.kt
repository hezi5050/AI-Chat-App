package com.hezi.chatsdk.di

import com.hezi.chatsdk.provider_router.AiChatProviderRouter
import com.hezi.chatsdk.AiChatSdk
import com.hezi.chatsdk.provider_router.ProviderRouter
import dagger.Binds
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

    @Provides
    @Singleton
    fun provideAiChatSdk(router: ProviderRouter, sdkConfig: SdkConfig): AiChatSdk {
        return AiChatSdk(router, sdkConfig)
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class SdkBindingModule {
    
    @Binds
    @Singleton
    abstract fun bindProviderRouter(impl: AiChatProviderRouter): ProviderRouter
}
