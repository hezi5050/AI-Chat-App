package com.hezi.aichatapp.commands

import android.content.Context
import com.hezi.aichatapp.R
import com.hezi.chatsdk.AiChatSdk
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Handles the execution of parsed commands
 */
@Singleton
class CommandHandler @Inject constructor(
    private val sdk: AiChatSdk,
    @ApplicationContext private val context: Context
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
                // Model already validated in parser
                sdk.updateConfiguration { copy(model = command.model) }
                CommandResult.Message(
                    context.getString(R.string.command_model_changed, command.model)
                )
            }

            is Command.ChangeProviderAndModel -> {
                // Provider and model already validated in parser
                sdk.updateConfiguration { 
                    copy(
                        providerName = command.providerName,
                        model = command.model
                    )
                }
                CommandResult.Message(
                    context.getString(
                        R.string.command_provider_and_model_changed,
                        command.providerName,
                        command.model
                    )
                )
            }

            is Command.ChangeTemperature -> {
                sdk.updateConfiguration { copy(temperature = command.temperature) }
                CommandResult.Message(
                    context.getString(R.string.command_temperature_set, command.temperature)
                )
            }

            is Command.ChangeMaxTokens -> {
                sdk.updateConfiguration { copy(maxTokens = command.maxTokens) }
                CommandResult.Message(
                    context.getString(R.string.command_tokens_set, command.maxTokens)
                )
            }

            is Command.ShowConfig -> {
                val config = sdk.getConfiguration()
                val provider = sdk.getAvailableProviders().find { it.name == config.providerName }
                val configText = buildString {
                    appendLine(context.getString(R.string.config_title))
                    appendLine(context.getString(R.string.config_provider, provider?.name ?: config.providerName))
                    appendLine(context.getString(R.string.config_model, config.model))
                    appendLine(context.getString(R.string.config_temperature, config.temperature))
                    appendLine(context.getString(R.string.config_max_tokens, config.maxTokens))
                    if (provider != null) {
                        appendLine()
                        appendLine(context.getString(R.string.config_available_models, provider.name))
                        provider.models.forEach { model ->
                            appendLine(context.getString(R.string.config_model_item, model))
                        }
                    }
                }
                CommandResult.Message(configText.trim())
            }

            is Command.Help -> {
                val availableProviders = sdk.getAvailableProviders()
                val helpText = buildString {
                    appendLine(context.getString(R.string.help_title))
                    appendLine(context.getString(R.string.help_clear))
                    appendLine(context.getString(R.string.help_model))
                    appendLine(context.getString(R.string.help_model_examples))
                    availableProviders.forEach { provider ->
                        val exampleModel = provider.models.firstOrNull() ?: "model-name"
                        appendLine(context.getString(R.string.help_model_example_item, provider.name, exampleModel))
                    }
                    appendLine(context.getString(R.string.help_temp))
                    appendLine(context.getString(R.string.help_tokens))
                    appendLine(context.getString(R.string.help_config))
                    appendLine(context.getString(R.string.help_help))
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

