# AI Chat App - Setup Instructions

## Getting Your API Key

To use this app, you need to add your OpenAI API key to `local.properties`:

1. Copy `local.properties.template` to `local.properties` (if it doesn't exist)
2. Get your API key from https://platform.openai.com/api-keys
3. Add it to `local.properties`:

```properties
OPENAI_API_KEY=sk-proj-your-actual-key-here
```

**Note**: `local.properties` is already in `.gitignore` and will NOT be committed to git.

## Building the Project

```bash
./gradlew clean assembleDebug
```

## Running the App

The app will automatically use the API key from `local.properties` when making OpenAI API calls.

