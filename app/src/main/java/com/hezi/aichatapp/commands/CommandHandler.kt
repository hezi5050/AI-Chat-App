package com.hezi.aichatapp.commands

interface CommandHandler {
    /**
     * Try to parse and handle a command from the given text
     * @param text The user input text
     * @return CommandResult indicating whether the command was handled and what action to take
     */
    fun handle(text: String): CommandResult

    /**
     * Result of command execution
     */
    sealed class CommandResult {
        /**
         * The text was not a command, continue with normal flow
         */
        data object NotHandled : CommandResult()

        /**
         * The command was handled successfully
         */
        sealed class Handled : CommandResult() {
            /**
             * Display a message to the user
             */
            data class Message(val text: String) : Handled()

            /**
             * Clear the conversation history
             */
            data object ClearHistory : Handled()
        }
    }
}
