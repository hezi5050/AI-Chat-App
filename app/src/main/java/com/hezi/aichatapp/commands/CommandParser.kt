package com.hezi.aichatapp.commands

interface CommandParser {
    /**
     * Parse a message string to determine if it's a command
     * @return Command if the message is a valid command, null otherwise
     */
    fun parse(message: String): Command?
}
