package com.hezi.aichatapp.data

import com.hezi.chatsdk.core.models.TokenUsage
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of DiagnosticsRepository for managing diagnostics data
 */
@Singleton
class ChatDiagnosticsRepository @Inject constructor() : DiagnosticsRepository {
    
    private val logsList = mutableListOf<LogInfo>()
    private var diagnosticsInfo = DiagnosticsInfo()

    /**
     * Record a successful request with response data
     */
    override fun recordSuccess(provider: String, model: String, latencyMs: Long, tokenUsage: TokenUsage?) {
        val newEntry = LogInfo.Success(
            provider = provider,
            model = model,
            latencyMs = latencyMs,
        )

        logsList.add(newEntry)
        
        val promptTokens = tokenUsage?.promptTokens ?: 0
        val completionTokens = tokenUsage?.completionTokens ?: 0
        val tokens = tokenUsage?.totalTokens ?: 0
        
        diagnosticsInfo = diagnosticsInfo.copy(
            totalRequests = diagnosticsInfo.totalRequests + 1,
            successfulRequests = diagnosticsInfo.successfulRequests + 1,
            totalPromptTokens = diagnosticsInfo.totalPromptTokens + promptTokens,
            totalCompletionTokens = diagnosticsInfo.totalCompletionTokens + completionTokens,
            totalTokens = diagnosticsInfo.totalTokens + tokens
        )
    }
    
    /**
     * Record a failed request with error information
     */
    override fun recordError(provider: String, model: String, errorMessage: String) {
        val newEntry = LogInfo.Error(
            provider = provider,
            model = model,
            errorMessage = errorMessage
        )

        logsList.add(newEntry)
        diagnosticsInfo = diagnosticsInfo.copy(
            totalRequests = diagnosticsInfo.totalRequests + 1,
            failedRequests = diagnosticsInfo.failedRequests + 1
        )
    }

    override fun getDiagnosticsInfo(): DiagnosticsInfo {
        return diagnosticsInfo
    }

    override fun getLogsList(): List<LogInfo> {
        return logsList.toList()
    }
}

