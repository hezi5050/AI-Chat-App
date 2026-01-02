package com.hezi.aichatapp.data

/**
 * Repository interface for managing diagnostics data
 */
interface DiagnosticsRepository {
    
    /**
     * Record a successful request with response data
     */
    fun recordSuccess(provider: String, model: String, latencyMs: Long)
    
    /**
     * Record a failed request with error information
     */
    fun recordError(provider: String, model: String, errorMessage: String)
    
    /**
     * Get aggregated diagnostics information
     */
    fun getDiagnosticsInfo(): DiagnosticsInfo
    
    /**
     * Get the complete list of log entries
     */
    fun getLogsList(): List<LogInfo>
}
