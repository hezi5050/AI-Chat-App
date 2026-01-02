package com.hezi.aichatapp.commands

import android.content.Context
import com.hezi.aichatapp.R
import com.hezi.chatsdk.AiChatSdk
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class ChatCommandHandler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val commandParser: CommandParser,
    private val sdk: AiChatSdk
) : CommandHandler {

    /**
     * Try to parse and handle a command from the given text
     * @param text The user input text
     * @return CommandResult indicating whether the command was handled and what action to take
     */
    override fun handle(text: String): CommandHandler.CommandResult {
        // Try to parse the text as a command
        val command = commandParser.parse(text) ?: return CommandHandler.CommandResult.NotHandled

        // Command was parsed, now handle it
        return handleCommand(command)
    }

    /**
     * Execute a parsed command and return a result message
     */
    private fun handleCommand(command: Command): CommandHandler.CommandResult.Handled {
        return when (command) {
            is Command.Clear -> {
                // The actual clearing is handled by the ViewModel
                CommandHandler.CommandResult.Handled.ClearHistory
            }

            is Command.ChangeModel -> {
                // Model already validated in parser
                sdk.updateConfiguration { copy(model = command.model) }
                CommandHandler.CommandResult.Handled.Message(
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
                CommandHandler.CommandResult.Handled.Message(
                    context.getString(
                        R.string.command_provider_and_model_changed,
                        command.providerName,
                        command.model
                    )
                )
            }

            is Command.ChangeTemperature -> {
                sdk.updateConfiguration { copy(temperature = command.temperature) }
                CommandHandler.CommandResult.Handled.Message(
                    context.getString(R.string.command_temperature_set, command.temperature)
                )
            }

            is Command.ChangeMaxTokens -> {
                sdk.updateConfiguration { copy(maxTokens = command.maxTokens) }
                CommandHandler.CommandResult.Handled.Message(
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
                CommandHandler.CommandResult.Handled.Message(configText.trim())
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
                CommandHandler.CommandResult.Handled.Message(helpText.trim())
            }

            is Command.Invalid -> {
                CommandHandler.CommandResult.Handled.Message(command.message)
            }
        }
    }
}

