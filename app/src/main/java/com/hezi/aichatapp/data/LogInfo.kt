package com.hezi.aichatapp.data

/**
 * Sealed class representing a log entry for diagnostics
 */
sealed class LogInfo {
    abstract val provider: String
    abstract val model: String

    /**
     * Represents a successful request log entry
     */
    data class Success(
        override val provider: String,
        override val model: String,
        val latencyMs: Long,
    ) : LogInfo()

    /**
     * Represents a failed request log entry
     */
    data class Error(
        override val provider: String,
        override val model: String,
        val errorMessage: String,
    ) : LogInfo()
}
