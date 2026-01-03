package com.hezi.aichatapp.commands

import android.content.Context
import com.hezi.aichatapp.R
import com.hezi.chatsdk.AiChatSdk
import com.hezi.chatsdk.core.config.Provider
import com.hezi.chatsdk.core.config.SdkConfiguration
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ChatCommandParserTest {

    private lateinit var parser: ChatCommandParser
    private lateinit var mockSdk: AiChatSdk
    private lateinit var mockContext: Context

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
        mockSdk = mockk(relaxed = true)
        mockContext = mockk(relaxed = true)

        // Mock context string resources
        every { mockContext.getString(R.string.command_usage_model) } returns "Usage: /model <model_name> or /model <provider:model>"
        every { mockContext.getString(R.string.command_usage_temperature) } returns "Usage: /temp <value> (0.0 - 2.0)"
        every { mockContext.getString(R.string.command_usage_tokens) } returns "Usage: /tokens <value>"
        every { mockContext.getString(R.string.command_unknown, *anyVararg()) } answers { 
            val args = (this.args[1] as Array<*>)
            val commandName = if (args.isNotEmpty()) args[0] else "unknown"
            "Unknown command: /$commandName. Type /help for available commands." 
        }
        every { mockContext.getString(R.string.command_invalid_provider, *anyVararg()) } answers {
            val args = (this.args[1] as Array<*>)
            val providerName = if (args.isNotEmpty()) args[0] else ""
            val availableNames = if (args.size > 1) args[1] else ""
            "Invalid provider: $providerName. Available providers: $availableNames"
        }
        every { mockContext.getString(R.string.command_invalid_model_for_provider, *anyVararg()) } answers {
            val args = (this.args[1] as Array<*>)
            val modelName = if (args.isNotEmpty()) args[0] else ""
            val providerName = if (args.size > 1) args[1] else ""
            val availableModels = if (args.size > 2) args[2] else ""
            "Invalid model '$modelName' for provider $providerName. Available models: $availableModels"
        }
        every { mockContext.getString(R.string.command_invalid_temperature_range) } returns "Temperature must be between 0.0 and 2.0"
        every { mockContext.getString(R.string.command_invalid_temperature_value, *anyVararg()) } answers {
            val args = (this.args[1] as Array<*>)
            val value = if (args.isNotEmpty()) args[0] else ""
            "Invalid temperature value: $value"
        }
        every { mockContext.getString(R.string.command_invalid_tokens_positive) } returns "Max tokens must be positive"
        every { mockContext.getString(R.string.command_invalid_tokens_value, *anyVararg()) } answers {
            val args = (this.args[1] as Array<*>)
            val value = if (args.isNotEmpty()) args[0] else ""
            "Invalid max tokens value: $value"
        }

        // Mock SDK methods
        every { mockSdk.getAvailableProviders() } returns listOf(openAiProvider, mockProvider)
        every { mockSdk.getProvider("OpenAI") } returns openAiProvider
        every { mockSdk.getProvider("Mock") } returns mockProvider
        every { mockSdk.getConfiguration() } returns SdkConfiguration(
            providerName = "OpenAI",
            model = "gpt-4o-mini",
            temperature = 0.7f,
            maxTokens = 1000
        )

        parser = ChatCommandParser(mockSdk, mockContext)
    }

    // Test non-commands
    @Test
    fun `parse returns null for non-command text`() {
        assertNull(parser.parse("Hello, how are you?"))
        assertNull(parser.parse("This is a regular message"))
        assertNull(parser.parse(""))
        assertNull(parser.parse("   "))
    }

    @Test
    fun `parse returns null for slash in middle of text`() {
        assertNull(parser.parse("This is a /test message"))
    }

    // Test /clear command
    @Test
    fun `parse clear command`() {
        val result = parser.parse("/clear")
        assertTrue(result is Command.Clear)
    }

    @Test
    fun `parse clear command with extra spaces`() {
        val result = parser.parse("  /clear  ")
        assertTrue(result is Command.Clear)
    }

    @Test
    fun `parse clear command case insensitive`() {
        val result = parser.parse("/CLEAR")
        assertTrue(result is Command.Clear)
    }

    // Test /model command - simple model change
    @Test
    fun `parse model command with valid model name`() {
        val result = parser.parse("/model gpt-4o")
        assertTrue(result is Command.ChangeModel)
        assertEquals("gpt-4o", (result as Command.ChangeModel).model)
    }

    @Test
    fun `parse model command with invalid model name`() {
        val result = parser.parse("/model invalid-model")
        assertTrue(result is Command.Invalid)
        assertTrue((result as Command.Invalid).message.contains("Invalid model"))
    }

    @Test
    fun `parse model command without arguments`() {
        val result = parser.parse("/model")
        assertTrue(result is Command.Invalid)
        assertTrue((result as Command.Invalid).message.contains("Usage"))
    }

    // Test /model command - provider:model format
    @Test
    fun `parse model command with provider and valid model`() {
        val result = parser.parse("/model OpenAI:gpt-4o")
        assertTrue(result is Command.ChangeProviderAndModel)
        assertEquals("OpenAI", (result as Command.ChangeProviderAndModel).providerName)
        assertEquals("gpt-4o", result.model)
    }

    @Test
    fun `parse model command with case-insensitive provider`() {
        val result = parser.parse("/model openai:gpt-4o")
        assertTrue(result is Command.ChangeProviderAndModel)
        assertEquals("OpenAI", (result as Command.ChangeProviderAndModel).providerName)
    }

    @Test
    fun `parse model command with invalid provider`() {
        val result = parser.parse("/model InvalidProvider:model")
        assertTrue(result is Command.Invalid)
        assertTrue((result as Command.Invalid).message.contains("Invalid provider"))
    }

    @Test
    fun `parse model command with provider but invalid model`() {
        val result = parser.parse("/model OpenAI:invalid-model")
        assertTrue(result is Command.Invalid)
        assertTrue((result as Command.Invalid).message.contains("Invalid model"))
    }

    @Test
    fun `parse model command with empty provider or model`() {
        val result1 = parser.parse("/model :gpt-4o")
        assertTrue(result1 is Command.Invalid)
        
        val result2 = parser.parse("/model OpenAI:")
        assertTrue(result2 is Command.Invalid)
    }

    // Test /temp command
    @Test
    fun `parse temperature command with valid value`() {
        val result = parser.parse("/temp 0.5")
        assertTrue(result is Command.ChangeTemperature)
        assertEquals(0.5f, (result as Command.ChangeTemperature).temperature)
    }

    @Test
    fun `parse temperature command with min value`() {
        val result = parser.parse("/temp 0.0")
        assertTrue(result is Command.ChangeTemperature)
        assertEquals(0.0f, (result as Command.ChangeTemperature).temperature)
    }

    @Test
    fun `parse temperature command with max value`() {
        val result = parser.parse("/temp 2.0")
        assertTrue(result is Command.ChangeTemperature)
        assertEquals(2.0f, (result as Command.ChangeTemperature).temperature)
    }

    @Test
    fun `parse temperature command with value too low`() {
        val result = parser.parse("/temp -0.1")
        assertTrue(result is Command.Invalid)
        assertTrue((result as Command.Invalid).message.contains("between 0.0 and 2.0"))
    }

    @Test
    fun `parse temperature command with value too high`() {
        val result = parser.parse("/temp 2.1")
        assertTrue(result is Command.Invalid)
        assertTrue((result as Command.Invalid).message.contains("between 0.0 and 2.0"))
    }

    @Test
    fun `parse temperature command with invalid format`() {
        val result = parser.parse("/temp abc")
        assertTrue(result is Command.Invalid)
        assertTrue((result as Command.Invalid).message.contains("Invalid temperature value"))
    }

    @Test
    fun `parse temperature command without arguments`() {
        val result = parser.parse("/temp")
        assertTrue(result is Command.Invalid)
        assertTrue((result as Command.Invalid).message.contains("Usage"))
    }

    @Test
    fun `parse temperature alias command`() {
        val result = parser.parse("/temperature 1.5")
        assertTrue(result is Command.ChangeTemperature)
        assertEquals(1.5f, (result as Command.ChangeTemperature).temperature)
    }

    // Test /tokens command
    @Test
    fun `parse tokens command with valid value`() {
        val result = parser.parse("/tokens 500")
        assertTrue(result is Command.ChangeMaxTokens)
        assertEquals(500, (result as Command.ChangeMaxTokens).maxTokens)
    }

    @Test
    fun `parse tokens command with large value`() {
        val result = parser.parse("/tokens 4000")
        assertTrue(result is Command.ChangeMaxTokens)
        assertEquals(4000, (result as Command.ChangeMaxTokens).maxTokens)
    }

    @Test
    fun `parse tokens command with zero value`() {
        val result = parser.parse("/tokens 0")
        assertTrue(result is Command.Invalid)
        assertTrue((result as Command.Invalid).message.contains("must be positive"))
    }

    @Test
    fun `parse tokens command with negative value`() {
        val result = parser.parse("/tokens -100")
        assertTrue(result is Command.Invalid)
        assertTrue((result as Command.Invalid).message.contains("must be positive"))
    }

    @Test
    fun `parse tokens command with invalid format`() {
        val result = parser.parse("/tokens xyz")
        assertTrue(result is Command.Invalid)
        assertTrue((result as Command.Invalid).message.contains("Invalid max tokens value"))
    }

    @Test
    fun `parse tokens command without arguments`() {
        val result = parser.parse("/tokens")
        assertTrue(result is Command.Invalid)
        assertTrue((result as Command.Invalid).message.contains("Usage"))
    }

    @Test
    fun `parse tokens alias command`() {
        val result = parser.parse("/maxtokens 1000")
        assertTrue(result is Command.ChangeMaxTokens)
        assertEquals(1000, (result as Command.ChangeMaxTokens).maxTokens)
    }

    // Test /config command
    @Test
    fun `parse config command`() {
        val result = parser.parse("/config")
        assertTrue(result is Command.ShowConfig)
    }

    @Test
    fun `parse config command with extra spaces`() {
        val result = parser.parse("  /config  ")
        assertTrue(result is Command.ShowConfig)
    }

    // Test /stats command
    @Test
    fun `parse stats command`() {
        val result = parser.parse("/stats")
        assertTrue(result is Command.ShowStats)
    }

    // Test /help command
    @Test
    fun `parse help command`() {
        val result = parser.parse("/help")
        assertTrue(result is Command.Help)
    }

    @Test
    fun `parse help command case insensitive`() {
        val result = parser.parse("/HELP")
        assertTrue(result is Command.Help)
    }

    // Test unknown commands
    @Test
    fun `parse unknown command`() {
        val result = parser.parse("/unknown")
        assertTrue(result is Command.Invalid)
        assertTrue((result as Command.Invalid).message.contains("Unknown command"))
    }

    @Test
    fun `parse invalid command with arguments`() {
        val result = parser.parse("/invalid arg1 arg2")
        assertTrue(result is Command.Invalid)
        assertTrue((result as Command.Invalid).message.contains("Unknown command"))
    }
}

