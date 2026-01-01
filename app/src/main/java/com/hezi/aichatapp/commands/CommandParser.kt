package com.hezi.aichatapp.commands

import android.content.Context
import com.hezi.aichatapp.R
import com.hezi.chatsdk.AiChatSdk
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Parses user input to identify and extract chat commands
 */
@Singleton
class CommandParser @Inject constructor(
    private val sdk: AiChatSdk,
    @ApplicationContext private val context: Context
) {

    /**
     * Parse a message string to determine if it's a command
     * @return Command if the message is a valid command, null otherwise
     */
    fun parse(message: String): Command? {
        val trimmed = message.trim()
        if (!trimmed.startsWith("/")) {
            return null // Not a command
        }

        val parts = trimmed.substring(1).split(" ", limit = 2)
        val commandName = parts[0].lowercase()
        val args = if (parts.size > 1) parts[1] else ""

        return when (commandName) {
            "clear" -> Command.Clear

            "model" -> {
                if (args.isBlank()) {
                    Command.Invalid(context.getString(R.string.command_usage_model))
                } else {
                    parseModel(args.trim())
                }
            }

            "temp", "temperature" -> {
                if (args.isBlank()) {
                    Command.Invalid(context.getString(R.string.command_usage_temperature))
                } else {
                    parseTemperature(args.trim())
                }
            }

            "tokens", "maxtokens" -> {
                if (args.isBlank()) {
                    Command.Invalid(context.getString(R.string.command_usage_tokens))
                } else {
                    parseMaxTokens(args.trim())
                }
            }

            "config" -> Command.ShowConfig

            "help" -> Command.Help

            else -> Command.Invalid(
                context.getString(R.string.command_unknown, commandName)
            )
        }
    }

    private fun parseModel(input: String): Command {
        // Check if it's in the format "provider:model"
        return if (input.contains(":")) {
            val parts = input.split(":", limit = 2)
            if (parts.size == 2) {
                val providerName = parts[0].trim()
                val modelName = parts[1].trim()
                
                if (providerName.isBlank() || modelName.isBlank()) {
                    Command.Invalid(context.getString(R.string.command_usage_model))
                } else {
                    // Validate provider exists (case-insensitive)
                    val availableProviders = sdk.getAvailableProviders()
                    val provider = availableProviders.find { it.name.equals(providerName, ignoreCase = true) }
                    
                    if (provider == null) {
                        val availableNames = availableProviders.joinToString(", ") { it.name }
                        return Command.Invalid(
                            context.getString(R.string.command_invalid_provider, providerName, availableNames)
                        )
                    }
                    
                    // Validate model exists in provider's model list (case-sensitive)
                    if (!provider.models.contains(modelName)) {
                        return Command.Invalid(
                            context.getString(
                                R.string.command_invalid_model_for_provider,
                                modelName,
                                provider.name,
                                provider.models.joinToString(", ")
                            )
                        )
                    }
                    
                    Command.ChangeProviderAndModel(provider.name, modelName)
                }
            } else {
                Command.Invalid(context.getString(R.string.command_usage_model))
            }
        } else {
            // Simple model name without provider - validate against current provider
            val currentConfig = sdk.getConfiguration()
            val currentProvider = sdk.getProvider(currentConfig.providerName)
            
            // Validate model exists in current provider's model list (case-sensitive)
            if (!currentProvider.models.contains(input)) {
                return Command.Invalid(
                    context.getString(
                        R.string.command_invalid_model_for_provider,
                        input,
                        currentProvider.name,
                        currentProvider.models.joinToString(", ")
                    )
                )
            }
            
            Command.ChangeModel(input)
        }
    }

    private fun parseTemperature(input: String): Command {
        return try {
            val temp = input.toFloat()
            if (temp < 0.0f || temp > 2.0f) {
                Command.Invalid(context.getString(R.string.command_invalid_temperature_range))
            } else {
                Command.ChangeTemperature(temp)
            }
        } catch (e: NumberFormatException) {
            Command.Invalid(
                context.getString(R.string.command_invalid_temperature_value, input)
            )
        }
    }

    private fun parseMaxTokens(input: String): Command {
        return try {
            val tokens = input.toInt()
            if (tokens <= 0) {
                Command.Invalid(context.getString(R.string.command_invalid_tokens_positive))
            } else {
                Command.ChangeMaxTokens(tokens)
            }
        } catch (e: NumberFormatException) {
            Command.Invalid(
                context.getString(R.string.command_invalid_tokens_value, input)
            )
        }
    }
}


