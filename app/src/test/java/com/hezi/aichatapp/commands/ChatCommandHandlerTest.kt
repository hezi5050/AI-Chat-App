package com.hezi.aichatapp.commands

import android.content.Context
import com.hezi.aichatapp.R
import com.hezi.aichatapp.data.DiagnosticsInfo
import com.hezi.aichatapp.data.DiagnosticsRepository
import com.hezi.aichatapp.data.LogInfo
import com.hezi.chatsdk.AiChatSdk
import com.hezi.chatsdk.core.config.Provider
import com.hezi.chatsdk.core.config.SdkConfiguration
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ChatCommandHandlerTest {

    private lateinit var handler: ChatCommandHandler
    private lateinit var mockContext: Context
    private lateinit var mockCommandParser: CommandParser
    private lateinit var mockSdk: AiChatSdk
    private lateinit var mockDiagnosticsRepository: DiagnosticsRepository

    private val openAiProvider = Provider(
        name = "OpenAI",
        models = listOf("gpt-4o", "gpt-4o-mini", "gpt-3.5-turbo")
    )

    private val mockProvider = Provider(
        name = "Mock",
        models = listOf("mock-model-1", "mock-model-2")
    )

    @Before
    fun setup() {
        mockContext = mockk(relaxed = true)
        mockCommandParser = mockk(relaxed = true)
        mockSdk = mockk(relaxed = true)
        mockDiagnosticsRepository = mockk(relaxed = true)

        // Mock string resources
        mockStringResources()

        // Mock SDK default configuration
        every { mockSdk.getConfiguration() } returns SdkConfiguration(
            providerName = "OpenAI",
            model = "gpt-4o-mini",
            temperature = 0.7f,
            maxTokens = 1000
        )
        every { mockSdk.getAvailableProviders() } returns listOf(openAiProvider, mockProvider)

        handler = ChatCommandHandler(mockContext, mockCommandParser, mockSdk, mockDiagnosticsRepository)
    }

    private fun mockStringResources() {
        every { mockContext.getString(R.string.command_model_changed, *anyVararg()) } answers {
            val args = (this.args[1] as Array<*>)
            val model = if (args.isNotEmpty()) args[0] else ""
            "Model changed to: $model"
        }
        every { mockContext.getString(R.string.command_provider_and_model_changed, *anyVararg()) } answers {
            val args = (this.args[1] as Array<*>)
            val provider = if (args.isNotEmpty()) args[0] else ""
            val model = if (args.size > 1) args[1] else ""
            "Provider changed to: $provider\nModel changed to: $model"
        }
        every { mockContext.getString(R.string.command_temperature_set, *anyVararg()) } answers {
            val args = (this.args[1] as Array<*>)
            val temp = if (args.isNotEmpty()) args[0] else ""
            "Temperature set to: $temp"
        }
        every { mockContext.getString(R.string.command_tokens_set, *anyVararg()) } answers {
            val args = (this.args[1] as Array<*>)
            val tokens = if (args.isNotEmpty()) args[0] else ""
            "Max tokens set to: $tokens"
        }
        every { mockContext.getString(R.string.config_title) } returns "Current Configuration:"
        every { mockContext.getString(R.string.config_provider, *anyVararg()) } answers {
            val args = (this.args[1] as Array<*>)
            val provider = if (args.isNotEmpty()) args[0] else ""
            "• Provider: $provider"
        }
        every { mockContext.getString(R.string.config_model, *anyVararg()) } answers {
            val args = (this.args[1] as Array<*>)
            val model = if (args.isNotEmpty()) args[0] else ""
            "• Model: $model"
        }
        every { mockContext.getString(R.string.config_temperature, *anyVararg()) } answers {
            val args = (this.args[1] as Array<*>)
            val temp = if (args.isNotEmpty()) args[0] else ""
            "• Temperature: $temp"
        }
        every { mockContext.getString(R.string.config_max_tokens, *anyVararg()) } answers {
            val args = (this.args[1] as Array<*>)
            val tokens = if (args.isNotEmpty()) args[0] else ""
            "• Max Tokens: $tokens"
        }
        every { mockContext.getString(R.string.config_available_models, *anyVararg()) } answers {
            val args = (this.args[1] as Array<*>)
            val provider = if (args.isNotEmpty()) args[0] else ""
            "Available models for $provider:"
        }
        every { mockContext.getString(R.string.config_model_item, *anyVararg()) } answers {
            val args = (this.args[1] as Array<*>)
            val model = if (args.isNotEmpty()) args[0] else ""
            "  - $model"
        }
        every { mockContext.getString(R.string.stats_title) } returns "Performance Statistics:"
        every { mockContext.getString(R.string.diagnostics_performance) } returns "Performance Metrics"
        every { mockContext.getString(R.string.stats_total_requests, *anyVararg()) } answers {
            val args = (this.args[1] as Array<*>)
            val count = if (args.isNotEmpty()) args[0] else ""
            "Total Requests: $count"
        }
        every { mockContext.getString(R.string.stats_successful, *anyVararg()) } answers {
            val args = (this.args[1] as Array<*>)
            val count = if (args.isNotEmpty()) args[0] else ""
            "Successful: $count"
        }
        every { mockContext.getString(R.string.stats_failed, *anyVararg()) } answers {
            val args = (this.args[1] as Array<*>)
            val count = if (args.isNotEmpty()) args[0] else ""
            "Failed: $count"
        }
        every { mockContext.getString(R.string.stats_prompt_tokens, *anyVararg()) } answers {
            val args = (this.args[1] as Array<*>)
            val tokens = if (args.isNotEmpty()) args[0] else ""
            "Prompt Tokens: $tokens"
        }
        every { mockContext.getString(R.string.stats_completion_tokens, *anyVararg()) } answers {
            val args = (this.args[1] as Array<*>)
            val tokens = if (args.isNotEmpty()) args[0] else ""
            "Completion Tokens: $tokens"
        }
        every { mockContext.getString(R.string.stats_total_tokens, *anyVararg()) } answers {
            val args = (this.args[1] as Array<*>)
            val tokens = if (args.isNotEmpty()) args[0] else ""
            "Total Tokens: $tokens"
        }
        every { mockContext.getString(R.string.diagnostics_logs_title) } returns "Request Logs"
        every { mockContext.getString(R.string.diagnostics_no_data) } returns "No logs available yet."
        every { mockContext.getString(R.string.stats_more_logs, *anyVararg()) } answers {
            val args = (this.args[1] as Array<*>)
            val count = if (args.isNotEmpty()) args[0] else ""
            "... and $count more logs (see Diagnostics screen for full history)"
        }
        every { mockContext.getString(R.string.help_title) } returns "Available Commands:"
        every { mockContext.getString(R.string.help_clear) } returns "/clear - Clear conversation history"
        every { mockContext.getString(R.string.help_model) } returns "/model <name> or /model <provider:model> - Change model"
        every { mockContext.getString(R.string.help_model_examples) } returns "  Examples:"
        every { mockContext.getString(R.string.help_model_example_item, *anyVararg()) } answers {
            val args = (this.args[1] as Array<*>)
            val provider = if (args.isNotEmpty()) args[0] else ""
            val model = if (args.size > 1) args[1] else ""
            "    /model $provider:$model"
        }
        every { mockContext.getString(R.string.help_temp) } returns "/temp <value> - Set temperature (0.0-2.0)"
        every { mockContext.getString(R.string.help_tokens) } returns "/tokens <value> - Set max tokens"
        every { mockContext.getString(R.string.help_config) } returns "/config - Show current configuration"
        every { mockContext.getString(R.string.help_stats) } returns "/stats - Show performance statistics"
        every { mockContext.getString(R.string.help_help) } returns "/help - Show this help message"
    }

    // Test NotHandled result
    @Test
    fun `handle returns NotHandled when parser returns null`() {
        every { mockCommandParser.parse("Hello") } returns null

        val result = handler.handle("Hello")

        assertTrue(result is CommandHandler.CommandResult.NotHandled)
    }

    // Test Clear command
    @Test
    fun `handle clear command returns ClearHistory`() {
        every { mockCommandParser.parse("/clear") } returns Command.Clear

        val result = handler.handle("/clear")

        assertTrue(result is CommandHandler.CommandResult.Handled.ClearHistory)
    }

    // Test ChangeModel command
    @Test
    fun `handle change model command updates configuration`() {
        every { mockCommandParser.parse("/model gpt-4o") } returns Command.ChangeModel("gpt-4o")
        justRun { mockSdk.updateConfiguration(any<SdkConfiguration.() -> SdkConfiguration>()) }

        val result = handler.handle("/model gpt-4o")

        assertTrue(result is CommandHandler.CommandResult.Handled.Message)
        assertEquals("Model changed to: gpt-4o", (result as CommandHandler.CommandResult.Handled.Message).text)
        verify { mockSdk.updateConfiguration(any<SdkConfiguration.() -> SdkConfiguration>()) }
    }

    // Test ChangeProviderAndModel command
    @Test
    fun `handle change provider and model command updates configuration`() {
        every { mockCommandParser.parse("/model Mock:mock-model-1") } returns 
            Command.ChangeProviderAndModel("Mock", "mock-model-1")
        justRun { mockSdk.updateConfiguration(any<SdkConfiguration.() -> SdkConfiguration>()) }

        val result = handler.handle("/model Mock:mock-model-1")

        assertTrue(result is CommandHandler.CommandResult.Handled.Message)
        val message = (result as CommandHandler.CommandResult.Handled.Message).text
        assertTrue(message.contains("Provider changed to: Mock"))
        assertTrue(message.contains("Model changed to: mock-model-1"))
        verify { mockSdk.updateConfiguration(any<SdkConfiguration.() -> SdkConfiguration>()) }
    }

    // Test ChangeTemperature command
    @Test
    fun `handle change temperature command updates configuration`() {
        every { mockCommandParser.parse("/temp 1.5") } returns Command.ChangeTemperature(1.5f)
        justRun { mockSdk.updateConfiguration(any<SdkConfiguration.() -> SdkConfiguration>()) }

        val result = handler.handle("/temp 1.5")

        assertTrue(result is CommandHandler.CommandResult.Handled.Message)
        assertEquals("Temperature set to: 1.5", (result as CommandHandler.CommandResult.Handled.Message).text)
        verify { mockSdk.updateConfiguration(any<SdkConfiguration.() -> SdkConfiguration>()) }
    }

    // Test ChangeMaxTokens command
    @Test
    fun `handle change max tokens command updates configuration`() {
        every { mockCommandParser.parse("/tokens 2000") } returns Command.ChangeMaxTokens(2000)
        justRun { mockSdk.updateConfiguration(any<SdkConfiguration.() -> SdkConfiguration>()) }

        val result = handler.handle("/tokens 2000")

        assertTrue(result is CommandHandler.CommandResult.Handled.Message)
        assertEquals("Max tokens set to: 2000", (result as CommandHandler.CommandResult.Handled.Message).text)
        verify { mockSdk.updateConfiguration(any<SdkConfiguration.() -> SdkConfiguration>()) }
    }

    // Test ShowConfig command
    @Test
    fun `handle show config command returns configuration details`() {
        every { mockCommandParser.parse("/config") } returns Command.ShowConfig

        val result = handler.handle("/config")

        assertTrue(result is CommandHandler.CommandResult.Handled.Message)
        val message = (result as CommandHandler.CommandResult.Handled.Message).text
        assertTrue(message.contains("Current Configuration:"))
        assertTrue(message.contains("Provider: OpenAI"))
        assertTrue(message.contains("Model: gpt-4o-mini"))
        assertTrue(message.contains("Temperature: 0.7"))
        assertTrue(message.contains("Max Tokens: 1000"))
        assertTrue(message.contains("Available models for OpenAI:"))
    }

    // Test ShowStats command
    @Test
    fun `handle show stats command with empty logs`() {
        every { mockCommandParser.parse("/stats") } returns Command.ShowStats
        every { mockDiagnosticsRepository.getDiagnosticsInfo() } returns DiagnosticsInfo(
            totalRequests = 0,
            successfulRequests = 0,
            failedRequests = 0,
            totalPromptTokens = 0,
            totalCompletionTokens = 0,
            totalTokens = 0
        )
        every { mockDiagnosticsRepository.getLogsList() } returns emptyList()

        val result = handler.handle("/stats")

        assertTrue(result is CommandHandler.CommandResult.Handled.Message)
        val message = (result as CommandHandler.CommandResult.Handled.Message).text
        assertTrue(message.contains("Performance Statistics:"))
        assertTrue(message.contains("Total Requests: 0"))
        assertTrue(message.contains("No logs available yet."))
    }

    @Test
    fun `handle show stats command with logs`() {
        every { mockCommandParser.parse("/stats") } returns Command.ShowStats
        every { mockDiagnosticsRepository.getDiagnosticsInfo() } returns DiagnosticsInfo(
            totalRequests = 3,
            successfulRequests = 2,
            failedRequests = 1,
            totalPromptTokens = 100,
            totalCompletionTokens = 200,
            totalTokens = 300
        )
        every { mockDiagnosticsRepository.getLogsList() } returns listOf(
            LogInfo.Success("OpenAI", "gpt-4o", 150),
            LogInfo.Error("OpenAI", "gpt-4o", "Network error"),
            LogInfo.Success("Mock", "mock-model-1", 50)
        )

        val result = handler.handle("/stats")

        assertTrue(result is CommandHandler.CommandResult.Handled.Message)
        val message = (result as CommandHandler.CommandResult.Handled.Message).text
        assertTrue(message.contains("Total Requests: 3"))
        assertTrue(message.contains("Successful: 2"))
        assertTrue(message.contains("Failed: 1"))
        assertTrue(message.contains("Prompt Tokens: 100"))
        assertTrue(message.contains("Completion Tokens: 200"))
        assertTrue(message.contains("Total Tokens: 300"))
        assertTrue(message.contains("Request Logs"))
        assertTrue(message.contains("provider: \"Mock\""))
        assertTrue(message.contains("latencyMs: 50"))
    }

    @Test
    fun `handle show stats command limits logs to 10`() {
        every { mockCommandParser.parse("/stats") } returns Command.ShowStats
        every { mockDiagnosticsRepository.getDiagnosticsInfo() } returns DiagnosticsInfo(
            totalRequests = 15,
            successfulRequests = 15,
            failedRequests = 0,
            totalPromptTokens = 0,
            totalCompletionTokens = 0,
            totalTokens = 0
        )
        val logs = (1..15).map { LogInfo.Success("OpenAI", "gpt-4o", (100 + it).toLong()) }
        every { mockDiagnosticsRepository.getLogsList() } returns logs

        val result = handler.handle("/stats")

        assertTrue(result is CommandHandler.CommandResult.Handled.Message)
        val message = (result as CommandHandler.CommandResult.Handled.Message).text
        assertTrue(message.contains("... and 5 more logs"))
    }

    // Test Help command
    @Test
    fun `handle help command returns help text`() {
        every { mockCommandParser.parse("/help") } returns Command.Help

        val result = handler.handle("/help")

        assertTrue(result is CommandHandler.CommandResult.Handled.Message)
        val message = (result as CommandHandler.CommandResult.Handled.Message).text
        assertTrue(message.contains("Available Commands:"))
        assertTrue(message.contains("/clear"))
        assertTrue(message.contains("/model"))
        assertTrue(message.contains("/temp"))
        assertTrue(message.contains("/tokens"))
        assertTrue(message.contains("/config"))
        assertTrue(message.contains("/stats"))
        assertTrue(message.contains("/help"))
        assertTrue(message.contains("Examples:"))
        assertTrue(message.contains("/model OpenAI:gpt-4o"))
    }

    // Test Invalid command
    @Test
    fun `handle invalid command returns error message`() {
        val errorMessage = "Usage: /model <model_name>"
        every { mockCommandParser.parse("/model") } returns Command.Invalid(errorMessage)

        val result = handler.handle("/model")

        assertTrue(result is CommandHandler.CommandResult.Handled.Message)
        assertEquals(errorMessage, (result as CommandHandler.CommandResult.Handled.Message).text)
    }
}

