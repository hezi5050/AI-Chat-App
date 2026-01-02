# Chat Commands Reference

This document describes all available chat commands in the AI Chat App.

## Overview

Commands allow you to configure and control the AI chat without leaving the conversation. All commands start with a forward slash (`/`).

## Available Commands

### `/help`

**Description:** Display help information about all available commands.

**Usage:**
```
/help
```

**Output:** Shows a list of all commands with their descriptions and examples.

---

### `/clear`

**Description:** Clear the current conversation history.

**Usage:**
```
/clear
```

**Effect:** Removes all messages from the chat interface and resets the conversation context.

---

### `/config`

**Description:** Display the current SDK configuration.

**Usage:**
```
/config
```

**Output:** Shows:
- Active provider name
- Current model
- Temperature setting
- Max tokens setting
- Available models for the current provider

**Example Output:**
```
Current Configuration:
• Provider: OpenAI
• Model: gpt-4o-mini
• Temperature: 0.7
• Max Tokens: 500

Available models for OpenAI:
  - gpt-4
  - gpt-4-turbo
  - gpt-4o
  - gpt-4o-mini
  - gpt-3.5-turbo
```

---

### `/model`

**Description:** Change the AI model for the current provider, or switch both provider and model at once.

**Usage:**
```
/model <model_name>
/model <provider>:<model>
```

**Parameters:**
- `model_name` - The exact name of the model to use (for current provider, case-sensitive)
- `provider:model` - Combined format to switch provider and model together (provider is case-insensitive, model is case-sensitive)

**Examples:**

*Change model for current provider:*
```
/model gpt-4o
/model gpt-3.5-turbo
/model default
```

*Switch provider and model together:*
```
/model OpenAI:gpt-4o-mini
/model Mock:default
/model openai:gpt-4
```

**Success Output (model only):**
```
Model changed to: gpt-4o
```

**Success Output (provider + model):**
```
Provider changed to: OpenAI
Model changed to: gpt-4o-mini
```

**Error Cases:**
- If model name is missing: `Usage: /model <model_name> or /model <provider>:<model>`
- If provider not found: `Invalid provider: xyz. Available providers: OpenAI, Mock`
- If model not found for provider: `Invalid model 'model-name' for provider OpenAI. Available models: gpt-4, gpt-4-turbo, ...`

**Important Notes:** 
- **Model names are case-sensitive** - You must use the exact model name as shown in `/config`
- Provider names are case-insensitive (e.g., `openai`, `OpenAI`, `OPENAI` all work)
- Use `/config` to see the exact list of available models for each provider
- When switching providers, you **must** specify a model (use `provider:model` format)

---

### `/temp` or `/temperature`

**Description:** Set the temperature parameter for AI responses.

**Usage:**
```
/temp <value>
/temperature <value>
```

**Parameters:**
- `value` - A number between 0.0 and 2.0

**Temperature Guide:**
- **0.0 - 0.2**: Very consistent, deterministic responses. Best for factual/structured outputs.
- **0.5 - 0.8**: Balanced. More natural and creative chat responses.
- **1.0 - 2.0**: Highly creative. More surprising and varied responses (may drift more).

**Examples:**
```
/temp 0.7
/temperature 1.2
```

**Success Output:**
```
Temperature set to: 0.7
```

**Error Cases:**
- If value is missing: `Usage: /temp <value> (0.0 - 2.0)`
- If value is out of range: `Temperature must be between 0.0 and 2.0`
- If value is not a number: `Invalid temperature value: xyz`

---

### `/tokens` or `/maxtokens`

**Description:** Set the maximum number of tokens for AI responses.

**Usage:**
```
/tokens <value>
/maxtokens <value>
```

**Parameters:**
- `value` - A positive integer

**Token Guide:**
- Lower values (100-500): Shorter, more concise responses
- Medium values (500-1000): Balanced response length
- Higher values (1000+): Longer, more detailed responses

**Examples:**
```
/tokens 1000
/maxtokens 500
```

**Success Output:**
```
Max tokens set to: 1000
```

**Error Cases:**
- If value is missing: `Usage: /tokens <value>`
- If value is not positive: `Max tokens must be positive`
- If value is not a number: `Invalid max tokens value: xyz`

**Note:** Higher values allow longer responses but may increase latency and cost.

---

### `/stats`
Displays performance statistics including total requests, successful/failed counts, and the 10 most recent request logs.

**Usage:**
```
/stats
```

**Example Output:**
```
Performance Statistics:

Performance Metrics
Total Requests: 15
Successful: 13
Failed: 2

Request Logs
{ provider: "openai", model: "gpt-4o-mini", latencyMs: 240 }
{ provider: "openai", model: "gpt-4o", latencyMs: 380 }
{ provider: "openai", model: "gpt-4o-mini", error: "Network timeout" }
... and 12 more logs (see Diagnostics screen for full history)
```

**Note:** Only the 10 most recent logs are shown. For complete history, open the Diagnostics screen from the menu.

---

## Command Features

### Case Sensitivity
- **Command names** are case-insensitive: `/help`, `/HELP`, `/Help` are all equivalent
- **Provider names** (in `provider:model` format) are case-insensitive: `OpenAI`, `openai`, `OPENAI` all work
- **Model names** are **case-sensitive**: You must use the exact model name (e.g., `gpt-4o`, not `GPT-4O` or `gpt-4O`)

### Aliases
Some commands have short aliases for convenience:
- `/temp` = `/temperature`
- `/tokens` = `/maxtokens`

### Validation
All commands perform strict validation:
- Provider names must match an available provider (case-insensitive)
- Model names must match exactly (case-sensitive) with the provider's model list
- Temperature must be between 0.0 and 2.0
- Max tokens must be a positive integer

### Error Handling
All commands provide helpful error messages if used incorrectly:
- Missing required parameters
- Invalid parameter values
- Out-of-range values
- Unknown commands
- **Model not found in provider's model list**

### Inline Feedback
Command results appear directly in the chat as system messages, making it easy to see what changed and verify your configuration.

---

## Example Workflows

### Quick Setup Workflow
```
# Switch provider and model together (one command!)
/model OpenAI:gpt-4o-mini

# Set creative temperature
/temp 1.0

# Increase token limit
/tokens 1000

# Verify changes
/config
```

### Testing Workflow
```
# Switch to mock provider with default model
/model Mock:default

# Test the chat
Hello, how are you?

# Clear and start fresh
/clear
```

### Switching Between Providers
```
# Quick switch between providers and models
/model OpenAI:gpt-4o-mini
Your question here...

/model Mock:default  
Test with mock provider...

/model OpenAI:gpt-4
Back to OpenAI with different model...
```

### Troubleshooting Workflow
```
# 1. Check if help is available
/help

# 2. View current settings and available models
/config

# 3. Switch to a valid model (use exact name from /config)
/model gpt-4o-mini

# 4. Reset to conservative settings
/temp 0.5
/tokens 500

# 5. Try again
Your question here...
```

---

## Common Mistakes

### ❌ Incorrect Model Case
```
/model GPT-4O        # Wrong - uppercase doesn't match
/model gpt-4O        # Wrong - mixed case doesn't match
```

### ✅ Correct Model Case
```
/model gpt-4o        # Correct - exact match
```

### ❌ Changing Provider Without Model
```
/provider OpenAI     # This command has been removed!
```

### ✅ Change Provider and Model Together
```
/model OpenAI:gpt-4o-mini    # Correct - provider and model together
```

### How to Find Correct Model Names
```
# Always use /config to see the exact model names
/config

# Then copy the exact model name
/model gpt-4o-mini   # Exact match from /config output
```

---

## Tips

1. **Use `/config` first** - Always check available models with `/config` before switching
2. **Copy model names exactly** - Model names are case-sensitive; use copy-paste when possible
3. **Use `provider:model` format** - Always specify both provider and model when switching providers
4. **Experiment with temperature** - Try different values to see what works best for your use case
5. **Adjust tokens based on need** - Use lower values for quick responses, higher for detailed explanations
6. **Use `/clear` strategically** - Start fresh when changing topics or testing different configurations

---

## Technical Notes

- Commands are executed immediately and do not require confirmation
- Command execution does not consume API tokens
- Configuration changes persist until the app is closed or changed again
- Commands appear in your chat history just like regular messages
- System responses (command results) are clearly distinguished from AI responses
- **Model validation is strict and case-sensitive to prevent runtime errors**
- **Provider changes must always include a valid model**

---

## Need Help?

- Type `/help` in the chat for a quick reference
- Type `/config` to see your current settings and available models
- Visit the Settings screen (☰ → Settings) for GUI-based configuration
- Check the app documentation for more detailed information

---

**Last Updated:** January 2026  
**Version:** 1.1 (Removed `/provider` command, added strict model validation)
