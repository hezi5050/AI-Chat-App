package com.hezi.aichatapp.commands

import com.hezi.chatsdk.core.config.Provider
import com.hezi.chatsdk.mock.di.MOCK_PROVIDER_NAME
import com.hezi.chatsdk.openai.di.OPEN_AI_PROVIDER_NAME

/**
 * Parses user input to identify and extract chat commands
 */
object CommandParser {

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
                    Command.Invalid("Usage: /model <model_name>")
                } else {
                    Command.ChangeModel(args.trim())
                }
            }

            "provider" -> {
                if (args.isBlank()) {
                    Command.Invalid("Usage: /provider <provider_id> (openai, mock)")
                } else {
                    parseProvider(args.trim())
                }
            }

            "temp", "temperature" -> {
                if (args.isBlank()) {
                    Command.Invalid("Usage: /temp <value> (0.0 - 2.0)")
                } else {
                    parseTemperature(args.trim())
                }
            }

            "tokens", "maxtokens" -> {
                if (args.isBlank()) {
                    Command.Invalid("Usage: /tokens <value>")
                } else {
                    parseMaxTokens(args.trim())
                }
            }

            "config" -> Command.ShowConfig

            "help" -> Command.Help

            else -> Command.Invalid("Unknown command: /$commandName. Type /help for available commands.")
        }
    }

    private fun parseProvider(input: String): Command {
        val providerId = input.lowercase()
        // Validate against known provider IDs
        return when (providerId) {
            OPEN_AI_PROVIDER_NAME, MOCK_PROVIDER_NAME -> Command.ChangeProvider(providerId)
            else -> Command.Invalid("Invalid provider: $input. Available: openai, mock")
        }
    }

    private fun parseTemperature(input: String): Command {
        return try {
            val temp = input.toFloat()
            if (temp < 0.0f || temp > 2.0f) {
                Command.Invalid("Temperature must be between 0.0 and 2.0")
            } else {
                Command.ChangeTemperature(temp)
            }
        } catch (e: NumberFormatException) {
            Command.Invalid("Invalid temperature value: $input")
        }
    }

    private fun parseMaxTokens(input: String): Command {
        return try {
            val tokens = input.toInt()
            if (tokens <= 0) {
                Command.Invalid("Max tokens must be positive")
            } else {
                Command.ChangeMaxTokens(tokens)
            }
        } catch (e: NumberFormatException) {
            Command.Invalid("Invalid max tokens value: $input")
        }
    }
}

