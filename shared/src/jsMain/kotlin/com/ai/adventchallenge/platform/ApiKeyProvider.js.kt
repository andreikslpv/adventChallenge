package com.ai.adventchallenge.platform

actual class ApiKeyProvider {
    actual fun getApiKey(): String {
        // For Web/JS, API key should be provided via environment variable or prompt
        // Set ZAI_API_KEY environment variable or prompt user to enter it
        // For testing, you can temporarily hardcode your key here:
        // return "c9190b80b0ef4dbd88949cb88e6bc458.YYIzsU456QFohz9y"
        
        return js("process.env.ZAI_API_KEY")?.toString() ?: ""
    }
}
