package com.hezi.aichatapp.commands

/**
 * Represents a parsed chat command
 */
sealed class Command {
    /**
     * Clear the conversation history
     */
    data object Clear : Command()

    /**
     * Change the AI model (for current provider)
     */
    data class ChangeModel(val model: String) : Command()

    /**
     * Change both provider and model together
     */
    data class ChangeProviderAndModel(val providerName: String, val model: String) : Command()

    /**
     * Change the temperature parameter
     */
    data class ChangeTemperature(val temperature: Float) : Command()

    /**
     * Change the max tokens parameter
     */
    data class ChangeMaxTokens(val maxTokens: Int) : Command()

    /**
     * Show current configuration
     */
    data object ShowConfig : Command()

    /**
     * Show available commands
     */
    data object Help : Command()

    /**
     * Invalid command
     */
    data class Invalid(val message: String) : Command()
}

