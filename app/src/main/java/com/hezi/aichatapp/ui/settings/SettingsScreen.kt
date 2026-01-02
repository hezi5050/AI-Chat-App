package com.hezi.aichatapp.ui.settings

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.hezi.aichatapp.R
import com.hezi.chatsdk.core.config.Provider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val availableProviders = viewModel.getAvailableProviders()
    val currentConfig = viewModel.getCurrentConfiguration()
    
    var selectedProvider by remember { 
        mutableStateOf(availableProviders.find { it.name == currentConfig.providerName })
    }
    var selectedModel by remember { mutableStateOf(currentConfig.model) }
    var isModelDropdownExpanded by remember { mutableStateOf(false) }
    var temperature by remember { mutableFloatStateOf(currentConfig.temperature) }
    var maxTokens by remember { mutableStateOf(currentConfig.maxTokens.toString()) }
    var isSaving by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()
    val showFadingEdge by remember {
        derivedStateOf {
            scrollState.value < scrollState.maxValue
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
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
        ) {
            // Scrollable content with fading edge
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(16.dp)
                ) {
                    ProviderSelectionSection(
                        availableProviders = availableProviders,
                        selectedProvider = selectedProvider,
                        onProviderSelected = { provider ->
                            selectedProvider = provider
                            selectedModel = provider.models.firstOrNull() ?: ""
                        }
                    )

                    ModelSelectionSection(
                        selectedProvider = selectedProvider,
                        selectedModel = selectedModel,
                        isDropdownExpanded = isModelDropdownExpanded,
                        onDropdownExpandedChange = { isModelDropdownExpanded = it },
                        onModelSelected = { selectedModel = it }
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    TemperatureSection(
                        temperature = temperature,
                        onTemperatureChange = { temperature = it }
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    MaxTokensSection(
                        maxTokens = maxTokens,
                        onMaxTokensChange = { maxTokens = it }
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Fading edge at the bottom
                if (showFadingEdge) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .height(40.dp)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        MaterialTheme.colorScheme.background
                                    )
                                )
                            )
                    )
                }
            }

            // Button below the scrollable content
            Button(
                onClick = {
                    if (!isSaving) {
                        isSaving = true
                        selectedProvider?.let { provider ->
                            val maxTokensValue = maxTokens.toIntOrNull() ?: currentConfig.maxTokens
                            viewModel.updateAllSettings(
                                provider = provider,
                                model = selectedModel,
                                temperature = temperature,
                                maxTokens = maxTokensValue
                            )
                            onNavigateBack()
                        }
                    }
                },
                enabled = !isSaving && 
                         selectedProvider != null && 
                         selectedModel.isNotBlank() &&
                         maxTokens.isNotEmpty() && 
                         maxTokens.toIntOrNull() != null && 
                         maxTokens.toInt() > 0,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(56.dp)
            ) {
                Text(
                    text = stringResource(R.string.save_settings),
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

@Composable
private fun ProviderSelectionSection(
    availableProviders: List<Provider>,
    selectedProvider: Provider?,
    onProviderSelected: (Provider) -> Unit
) {
    val context = LocalContext.current
    Text(
        text = stringResource(R.string.ai_provider),
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(bottom = 16.dp)
    )
    
    availableProviders.forEach { provider ->
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (selectedProvider?.name == provider.name) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                }
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = selectedProvider?.name == provider.name,
                    onClick = { onProviderSelected(provider) }
                )
                Column(modifier = Modifier.padding(start = 8.dp)) {
                    Text(
                        text = provider.name,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = context.resources.getQuantityString(
                            R.plurals.models_available,
                            provider.models.size,
                            provider.models.size
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModelSelectionSection(
    selectedProvider: Provider?,
    selectedModel: String,
    isDropdownExpanded: Boolean,
    onDropdownExpandedChange: (Boolean) -> Unit,
    onModelSelected: (String) -> Unit
) {
    selectedProvider?.let { provider ->
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = stringResource(R.string.model),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        ExposedDropdownMenuBox(
            expanded = isDropdownExpanded,
            onExpandedChange = onDropdownExpandedChange
        ) {
            TextField(
                value = selectedModel,
                onValueChange = {},
                readOnly = true,
                label = { Text(stringResource(R.string.select_model)) },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = isDropdownExpanded)
                },
                colors = ExposedDropdownMenuDefaults.textFieldColors(),
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )

            ExposedDropdownMenu(
                expanded = isDropdownExpanded,
                onDismissRequest = { onDropdownExpandedChange(false) }
            ) {
                provider.models.forEach { model ->
                    DropdownMenuItem(
                        text = { 
                            Text(
                                text = model,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        },
                        onClick = {
                            onModelSelected(model)
                            onDropdownExpandedChange(false)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun TemperatureSection(
    temperature: Float,
    onTemperatureChange: (Float) -> Unit
) {
    Text(
        text = stringResource(R.string.temperature),
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(bottom = 8.dp)
    )
    
    Text(
        text = getTemperatureDescription(temperature),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        minLines = 2,
        modifier = Modifier.padding(bottom = 8.dp)
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.temperature_min),
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(end = 8.dp)
        )
        Slider(
            value = temperature,
            onValueChange = onTemperatureChange,
            valueRange = 0f..2f,
            steps = 19, // 0.1 increments
            colors = SliderDefaults.colors(
                thumbColor = getTemperatureColor(temperature),
                activeTrackColor = getTemperatureColor(temperature)
            ),
            modifier = Modifier.weight(1f)
        )
        Text(
            text = stringResource(R.string.temperature_max),
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
    
    Text(
        text = stringResource(R.string.temperature_current, temperature),
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 4.dp)
    )
}

@Composable
private fun MaxTokensSection(
    maxTokens: String,
    onMaxTokensChange: (String) -> Unit
) {
    Text(
        text = stringResource(R.string.max_tokens),
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(bottom = 8.dp)
    )
    
    Text(
        text = stringResource(R.string.max_tokens_description),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(bottom = 12.dp)
    )

    OutlinedTextField(
        value = maxTokens,
        onValueChange = { 
            // Only allow numeric input
            if (it.isEmpty() || it.all { char -> char.isDigit() }) {
                onMaxTokensChange(it)
            }
        },
        label = { Text(stringResource(R.string.max_tokens)) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        isError = maxTokens.isEmpty() || maxTokens.toIntOrNull() == null || maxTokens.toInt() <= 0,
        supportingText = {
            if (maxTokens.isEmpty() || maxTokens.toIntOrNull() == null || maxTokens.toInt() <= 0) {
                Text(stringResource(R.string.max_tokens_error), color = MaterialTheme.colorScheme.error)
            }
        },
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun getTemperatureDescription(temperature: Float): String {
    return when {
        temperature < 0.3f -> stringResource(R.string.temperature_consistent)
        temperature < 0.9f -> stringResource(R.string.temperature_balanced)
        else -> stringResource(R.string.temperature_creative)
    }
}

@Composable
private fun getTemperatureColor(temperature: Float): Color {
    return when {
        temperature < 0.3f -> Color(0xFF4CAF50) // Green - Consistent
        temperature < 0.9f -> Color(0xFF2196F3) // Blue - Balanced
        else -> Color(0xFFFF9800) // Orange - Creative
    }
}
