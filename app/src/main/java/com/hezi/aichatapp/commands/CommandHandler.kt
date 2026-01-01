package com.hezi.aichatapp.commands

import com.hezi.chatsdk.AiChatSdk
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Handles the execution of parsed commands
 */
@Singleton
class CommandHandler @Inject constructor(
    private val sdk: AiChatSdk
) {

    /**
     * Execute a command and return a result message
     */
    fun handle(command: Command): CommandResult {
        return when (command) {
            is Command.Clear -> {
                // The actual clearing is handled by the ViewModel
                CommandResult.ClearHistory
            }

            is Command.ChangeModel -> {
                sdk.updateConfiguration { copy(model = command.model) }
                CommandResult.Message("Model changed to: ${command.model}")
            }

            is Command.ChangeProvider -> {
                val provider = sdk.getAvailableProviders().find { it.name == command.provider }
                if (provider != null) {
                    sdk.updateConfiguration { copy(providerName = command.provider) }
                    CommandResult.Message("Provider changed to: ${provider.name}\nAvailable models: ${provider.models.joinToString(", ")}")
                } else {
                    CommandResult.Message("Provider '${command.provider}' not found")
                }
            }

            is Command.ChangeTemperature -> {
                sdk.updateConfiguration { copy(temperature = command.temperature) }
                CommandResult.Message("Temperature set to: ${command.temperature}")
            }

            is Command.ChangeMaxTokens -> {
                sdk.updateConfiguration { copy(maxTokens = command.maxTokens) }
                CommandResult.Message("Max tokens set to: ${command.maxTokens}")
            }

            is Command.ShowConfig -> {
                val config = sdk.getConfiguration()
                val provider = sdk.getAvailableProviders().find { it.name == config.providerName }
                val configText = buildString {
                    appendLine("Current Configuration:")
                    appendLine("• Provider: ${provider?.name ?: config.providerName}")
                    appendLine("• Model: ${config.model}")
                    appendLine("• Temperature: ${config.temperature}")
                    appendLine("• Max Tokens: ${config.maxTokens}")
                    if (provider != null) {
                        appendLine("\nAvailable models for ${provider.name}:")
                        provider.models.forEach { model ->
                            appendLine("  - $model")
                        }
                    }
                }
                CommandResult.Message(configText.trim())
            }

            is Command.Help -> {
                val availableProviders = sdk.getAvailableProviders()
                val helpText = buildString {
                    appendLine("Available Commands:")
                    appendLine("/clear - Clear conversation history")
                    appendLine("/model <name> - Change AI model")
                    appendLine("/provider <id> - Switch provider")
                    appendLine("  Available providers:")
                    availableProviders.forEach { provider ->
                        appendLine("    • ${provider.name} - ${provider.name}")
                    }
                    appendLine("/temp <value> - Set temperature (0.0-2.0)")
                    appendLine("/tokens <value> - Set max tokens")
                    appendLine("/config - Show current configuration")
                    appendLine("/help - Show this help message")
                }
                CommandResult.Message(helpText.trim())
            }

            is Command.Invalid -> {
                CommandResult.Message(command.message)
            }
        }
    }

    /**
     * Result of command execution
     */
    sealed class CommandResult {
        /**
         * Display a message to the user
         */
        data class Message(val text: String) : CommandResult()

        /**
         * Clear the conversation history
         */
        data object ClearHistory : CommandResult()
    }
}

