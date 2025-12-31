package com.hezi.aichatapp

import android.app.Application
import com.hezi.chatsdk.AiChatSdk
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class AiChatApplication : Application() {
    
    @Inject
    lateinit var aiChatSdk: AiChatSdk
    
    override fun onCreate() {
        super.onCreate()
        
        // Set debug mode based on build configuration
        aiChatSdk.setDebugMode(BuildConfig.DEBUG)
    }
}

