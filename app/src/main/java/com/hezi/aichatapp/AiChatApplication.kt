package com.hezi.aichatapp

import android.app.Application
import com.hezi.chatsdk.AiChatSdk
import com.hezi.chatsdk.core.config.SdkConfiguration
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class AiChatApplication : Application() {
    
    @Inject
    lateinit var aiChatSdk: AiChatSdk
    
    override fun onCreate() {
        super.onCreate()

        initializeSdk()
    }

    private fun initializeSdk() {
        // Set debug mode based on build configuration
        aiChatSdk.setDebugMode(BuildConfig.DEBUG)

        // Initialize default configuration
        aiChatSdk.updateConfiguration(SdkConfiguration())
    }
}

