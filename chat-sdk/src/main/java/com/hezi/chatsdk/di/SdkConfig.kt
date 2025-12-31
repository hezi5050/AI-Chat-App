package com.hezi.chatsdk.di

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Runtime configuration for the SDK.
 * Contains only runtime flags, not sensitive data like API keys.
 */
@Singleton
class SdkConfig @Inject constructor() {
    
    var isDebug: Boolean = false
        private set
    
    fun setDebugMode(isDebug: Boolean) {
        this.isDebug = isDebug
    }
}

