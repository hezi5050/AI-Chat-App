# AI Chat App - Multi-Module SDK Architecture

## Overview

A modular Android AI chat application with a custom SDK that abstracts multiple LLM providers (OpenAI, Mock). The SDK is split into separate modules for better separation of concerns, testability, and maintainability.

## Module Structure

```
ai-chat-app/
├── app/                      # Main Android application
├── chat-sdk-core/            # Core contracts and interfaces
├── chat-sdk-openai/          # OpenAI implementation
├── chat-sdk-mock/            # Mock implementation for testing
└── chat-sdk/                 # Aggregator SDK (public API)
```

### 1. `chat-sdk-core` (Contracts Module)

**Purpose**: Defines all interfaces, data models, and contracts that provider modules must implement.

**Key Components**:
- `LlmProvider` interface - Base contract for all LLM providers
- `Provider` enum - Type-safe provider identifiers (OPENAI, MOCK)
- `SdkConfiguration` - Runtime configuration data class
- Data models: `ChatMessage`, `ChatRequest`, `ChatResponse`, `StreamEvent`, `TokenUsage`

**Dependencies**: 
- Kotlin Coroutines (for Flow)
- Hilt (for DI annotations)

**Package**: `com.hezi.chatsdk.core`

**Structure**:
```
chat-sdk-core/
└── src/main/java/com/hezi/chatsdk/core/
    ├── config/
    │   ├── Provider.kt
    │   └── SdkConfiguration.kt
    ├── models/
    │   ├── ChatMessage.kt
    │   ├── ChatRequest.kt
    │   ├── ChatResponse.kt
    │   └── StreamEvent.kt
    └── providers/
        └── LlmProvider.kt
```

### 2. `chat-sdk-openai` (OpenAI Implementation)

**Purpose**: Implements `LlmProvider` interface for OpenAI's API.

**Key Components**:
- `OpenAiProvider` - Implements both standard and streaming chat completions
- `OpenAiApiService` - Retrofit interface for OpenAI API
- `ApiKeyModule` - Provides hardcoded API key (for demo purposes)
- `@OpenAiApiKey` - Qualifier annotation for API key injection
- OpenAI-specific models: `OpenAiChatRequest`, `OpenAiChatResponse`, etc.

**Dependencies**:
- `chat-sdk-core` (api)
- Retrofit, OkHttp, Kotlin Serialization
- Hilt (for DI integration)

**Package**: `com.hezi.chatsdk.openai`

**Structure**:
```
chat-sdk-openai/
└── src/main/java/com/hezi/chatsdk/openai/
    ├── di/
    │   ├── ApiKeyModule.kt       # Provides hardcoded API key
    │   └── OpenAiApiKey.kt       # Qualifier annotation
    ├── OpenAiProvider.kt
    ├── OpenAiApiService.kt
    └── OpenAiModels.kt
```

**Features**:
- Non-streaming chat completions
- Streaming chat completions (Server-Sent Events)
- Token usage tracking
- Latency measurement
- Uses configuration (model, temperature, maxTokens) from SdkConfiguration

### 3. `chat-sdk-mock` (Mock Implementation)

**Purpose**: Provides a mock implementation for testing and development without hitting real APIs.

**Key Components**:
- `MockProvider` - Simulates LLM responses with configurable delays

**Dependencies**:
- `chat-sdk-core` (api)
- Kotlin Coroutines

**Package**: `com.hezi.chatsdk.mock`

**Structure**:
```
chat-sdk-mock/
└── src/main/java/com/hezi/chatsdk/mock/
    └── MockProvider.kt
```

**Features**:
- Instant mock responses
- Streaming simulation
- Context-aware mock replies
- Simulated token usage
- Configurable latency

### 4. `chat-sdk` (Aggregator Module)

**Purpose**: Public-facing SDK that aggregates all provider implementations and provides networking infrastructure. This is the module that applications depend on.

**Key Components**:
- `AiChatSdk` - Main SDK interface
- `ProviderRouter` - Routes requests to appropriate provider based on configuration
- `NetworkModule` - Provides Retrofit, OkHttp, JSON serialization
- `ProviderModule` - Binds Mock provider
- `SdkConfig` - Runtime configuration (debug mode)
- `@ProviderKey` - MapKey annotation for provider multibinding

**Dependencies**:
- `chat-sdk-core` (api)
- `chat-sdk-openai` (api)
- `chat-sdk-mock` (api)
- Retrofit, OkHttp, Kotlin Serialization
- Hilt (for DI)

**Package**: `com.hezi.chatsdk`

**Structure**:
```
chat-sdk/
└── src/main/java/com/hezi/chatsdk/
    ├── AiChatSdk.kt
    ├── ProviderRouter.kt
    └── di/
        ├── NetworkModule.kt      # Retrofit, OkHttp, JSON
        ├── ProviderModule.kt     # Binds Mock provider
        ├── ProviderKey.kt        # MapKey for multibinding
        ├── SdkConfig.kt          # Runtime config
        └── SdkModule.kt          # Provides SdkConfig
```

**Features**:
- Dynamic provider switching
- Configuration management
- Unified API for all providers
- Networking infrastructure

### 5. `app` (Android Application)

**Purpose**: Main Android application that consumes the Chat SDK.

**Key Components**:
- `AiChatApplication` - Hilt application class, sets debug mode
- `MainActivity` - Compose host activity

**Dependencies**:
- `chat-sdk` (implementation)
- Compose UI, Material3
- Hilt, Room (for future persistence)
- Navigation Compose (for future screens)

**Structure**:
```
app/
└── src/main/java/com/hezi/aichatapp/
    ├── AiChatApplication.kt
    ├── MainActivity.kt
    └── di/                      # Empty - no app-specific DI
```

## Dependency Injection Architecture

### Provider Multibinding

The SDK uses Hilt's multibinding to create a `Map<Provider, LlmProvider>` that is injected into `ProviderRouter`:

**In chat-sdk-openai/di/ApiKeyModule.kt:**
```kotlin
@Provides
@Singleton
@OpenAiApiKey
fun provideOpenAiApiKey(): String {
    return "sk-proj-..." // Hardcoded for demo
}
```

**In chat-sdk/di/ProviderModule.kt:**
```kotlin
@Provides
@IntoMap
@ProviderKey(Provider.OPENAI)
@Singleton
fun provideOpenAiProvider(
    apiService: OpenAiApiService,
    json: Json,
    @OpenAiApiKey apiKey: String
): LlmProvider {
    return OpenAiProvider(...)
}

@Provides
@IntoMap
@ProviderKey(Provider.MOCK)
@Singleton
fun provideMockProvider(): LlmProvider {
    return MockProvider()
}
```

**The ProviderRouter receives this map automatically:**
```kotlin
@Singleton
class ProviderRouter @Inject constructor(
    private val providers: Map<Provider, @JvmSuppressWildcards LlmProvider>
) {
    // Routes to appropriate provider based on configuration
}
```

## Configuration Management

### SdkConfiguration

```kotlin
data class SdkConfiguration(
    val provider: Provider = Provider.OPENAI,
    val model: String = "gpt-4o-mini",
    val temperature: Float = 0.7f,
    val maxTokens: Int = 500
)
```

**Key Points**:
- ✅ Configuration parameters (temperature, maxTokens) are passed to providers via `SdkConfiguration`
- ✅ Each `chat()` and `chatStream()` call receives the current configuration
- ✅ No parameters duplicated in `ChatRequest` - it only contains messages

### API Key Management

API keys are managed at the **provider SDK level** for demo purposes:
- Hardcoded in `chat-sdk-openai/di/ApiKeyModule.kt`
- Provided via `@OpenAiApiKey` qualifier
- Can be easily changed to use BuildConfig or encrypted storage in production

## Usage Example

```kotlin
@HiltViewModel
class ChatViewModel @Inject constructor(
    private val sdk: AiChatSdk
) : ViewModel() {
    
    init {
        // Configure SDK
        sdk.updateConfiguration { 
            copy(
                provider = Provider.OPENAI,
                model = "gpt-4o-mini",
                temperature = 0.8f,
                maxTokens = 1000
            ) 
        }
    }
    
    fun sendMessage(userMessage: String) {
        viewModelScope.launch {
            val request = ChatRequest(
                messages = listOf(
                    ChatMessage(MessageRole.USER, userMessage)
                )
            )
            
            // Non-streaming
            val response = sdk.chat(request)
            
            // Or streaming
            sdk.chatStream(request).collect { event ->
                when (event) {
                    is StreamEvent.Delta -> handleDelta(event.text)
                    is StreamEvent.Complete -> handleComplete(event.response)
                    is StreamEvent.Error -> handleError(event.error)
                }
            }
        }
    }
}
```

## Build Configuration

### Version Catalog (`gradle/libs.versions.toml`)

All dependencies are centrally managed in the version catalog:
- Compose BOM: 2025.01.00
- Kotlin: 1.9.23
- Hilt: 2.51
- Retrofit: 2.11.0
- Room: 2.6.1
- Coroutines: 1.8.0

### Module Build Files

All modules use consistent configuration:
- `compileSdk 35`
- `minSdk 24`
- `jvmTarget '17'`
- Proguard rules for release builds

## API Design Principles

1. **Simplicity** - Clean, intuitive APIs with sensible defaults
2. **Type Safety** - Enums instead of strings (Provider enum)
3. **Encapsulation** - API keys managed by provider modules
4. **Testability** - Mock provider for testing without network calls
5. **Extensibility** - Easy to add new providers by implementing `LlmProvider`
6. **Thread Safety** - `@Volatile` configuration, suspend functions, Flow for streaming
7. **Configuration-Driven** - Request parameters come from `SdkConfiguration`, not `ChatRequest`

## Architecture Flow

```
app module
    ↓ depends on
chat-sdk (aggregator)
    ├── NetworkModule (provides Retrofit, OkHttp, JSON)
    ├── ProviderModule (binds Mock provider)
    └── Injects Map<Provider, LlmProvider>
    ↓
ProviderRouter
    ├── Manages SdkConfiguration
    ├── Routes requests to providers
    └── Passes configuration to each provider
    ↓
Individual Providers
    ├── chat-sdk-openai (provides OpenAI + API key)
    └── chat-sdk-mock (provides Mock)
    ↓
All depend on chat-sdk-core
    └── Interfaces, models, annotations
```

## Next Steps

### Immediate (Step 4 onwards):
1. ✅ ~~Create mock provider for testing~~ **COMPLETED**
2. Build command parser and handler
3. Implement Room database and repository
4. Build ChatScreen, SettingsScreen, DiagnosticsScreen
5. Wire ViewModel with SDK, commands, persistence
6. Set up Compose navigation and update MainActivity
7. Add unit tests
8. Create README documentation

### Future Enhancements:
- Add more providers (Anthropic, Google AI, etc.)
- Implement conversation history management
- Add token counting utilities
- Provider health checking and fallbacks
- Rate limiting and retry logic
- Caching layer
- Encrypted API key storage for production

## Testing Strategy

### Unit Tests
- Test each provider independently
- Test ProviderRouter logic
- Test configuration management
- Mock network responses for OpenAI tests

### Integration Tests
- Test SDK with mock provider
- Test provider switching
- Test streaming functionality

### UI Tests
- Test chat screen interactions
- Test settings changes
- Test command parsing

## Architecture Benefits

✅ **Separation of Concerns** - Each module has a single responsibility

✅ **Testability** - Easy to test individual modules in isolation

✅ **Maintainability** - Changes to one provider don't affect others

✅ **Extensibility** - New providers can be added without modifying existing code

✅ **Type Safety** - Compile-time safety with Provider enum

✅ **Dependency Management** - Clear dependency graph prevents circular dependencies

✅ **Reusability** - Core module can be shared across different projects

✅ **Configuration-Driven** - Centralized configuration management

✅ **Provider Independence** - Each provider module is self-contained

## Build Status

✅ **All modules compile successfully**
✅ **Hilt dependency injection configured**
✅ **Multi-module architecture implemented**
✅ **OpenAI and Mock providers ready**
✅ **Configuration properly flows to providers**

---

**Last Updated**: December 31, 2025  
**Build**: Success  
**Next Milestone**: Command Parser & Handler (Step 5)
