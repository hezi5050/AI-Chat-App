package com.hezi.aichatapp.ui.diagnostics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.hezi.aichatapp.R
import com.hezi.aichatapp.data.LogInfo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiagnosticsScreen(
    onNavigateBack: () -> Unit,
    viewModel: DiagnosticsViewModel = hiltViewModel()
) {
    val diagnosticsInfo = viewModel.getDiagnosticsInfo()
    val logsList = viewModel.getLogsList()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.diagnostics_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back_button_description)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            
            // Performance Metrics Section
            Text(
                text = stringResource(R.string.diagnostics_performance),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(16.dp)
            ) {
                MetricRow(
                    label = stringResource(R.string.diagnostics_total_requests),
                    value = diagnosticsInfo.totalRequests.toString()
                )
                Spacer(modifier = Modifier.height(8.dp))
                MetricRow(
                    label = stringResource(R.string.diagnostics_successful),
                    value = diagnosticsInfo.successfulRequests.toString()
                )
                Spacer(modifier = Modifier.height(8.dp))
                MetricRow(
                    label = stringResource(R.string.diagnostics_failed),
                    value = diagnosticsInfo.failedRequests.toString()
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Logs Section
            Text(
                text = stringResource(R.string.diagnostics_logs_title),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            if (logsList.isEmpty()) {
                Text(
                    text = stringResource(R.string.diagnostics_no_data),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                // Display each log entry (most recent first)
                logsList.reversed().forEach { logInfo ->
                    LogEntry(logInfo = logInfo)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun MetricRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun LogEntry(logInfo: LogInfo) {
    val isError = logInfo is LogInfo.Error
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = if (isError) 
                    MaterialTheme.colorScheme.errorContainer 
                else 
                    MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(16.dp)
    ) {
        // Display log info in JSON-like format
        val logText = when (logInfo) {
            is LogInfo.Success -> {
                buildString {
                    append("{ ")
                    append("provider: \"${logInfo.provider}\", ")
                    append("model: \"${logInfo.model}\", ")
                    append("latencyMs: ${logInfo.latencyMs}")
                    append(" }")
                }
            }
            is LogInfo.Error -> {
                buildString {
                    append("{ ")
                    append("provider: \"${logInfo.provider}\", ")
                    append("model: \"${logInfo.model}\", ")
                    append("error: \"${logInfo.errorMessage}\"")
                    append(" }")
                }
            }
        }

        Text(
            text = logText,
            style = MaterialTheme.typography.bodySmall,
            fontFamily = FontFamily.Monospace,
            color = if (isError) 
                MaterialTheme.colorScheme.onErrorContainer 
            else 
                MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

