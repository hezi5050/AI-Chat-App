package com.hezi.aichatapp.ui.settings

import androidx.lifecycle.ViewModel
import com.hezi.chatsdk.AiChatSdk
import com.hezi.chatsdk.core.config.Provider
import com.hezi.chatsdk.core.config.SdkConfiguration
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val sdk: AiChatSdk
) : ViewModel() {

    fun getAvailableProviders(): List<Provider> {
        return sdk.getAvailableProviders()
    }

    fun getCurrentConfiguration(): SdkConfiguration {
        return sdk.getConfiguration()
    }

    fun updateProviderAndModel(provider: Provider, model: String) {
        sdk.updateConfiguration { 
            copy(
                providerName = provider.name,
                model = model
            )
        }
    }

    fun updateAllSettings(
        provider: Provider,
        model: String,
        temperature: Float,
        maxTokens: Int
    ) {
        sdk.updateConfiguration {
            copy(
                providerName = provider.name,
                model = model,
                temperature = temperature,
                maxTokens = maxTokens
            )
        }
    }
}

