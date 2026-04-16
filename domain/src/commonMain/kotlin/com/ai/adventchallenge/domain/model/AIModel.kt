package com.ai.adventchallenge.domain.model

enum class AIProvider(
    val displayName: String,
    val baseUrl: String,
    val apiKeyHeaderName: String,
    val defaultModel: String
) {
    ZAI(
        displayName = "z.ai",
        baseUrl = "https://api.z.ai/api/paas/v4/chat/completions",
        apiKeyHeaderName = "Authorization",
        defaultModel = "glm-4.7"
    ),
    OPENAI(
        displayName = "OpenAI",
        baseUrl = "https://api.openai.com/v1/chat/completions",
        apiKeyHeaderName = "Authorization",
        defaultModel = "gpt-4o"
    )
}

data class AIModel(
    val provider: AIProvider,
    val modelName: String,
    val displayName: String = "${provider.displayName}: $modelName",
    val maxContextWindow: Int = 200000
) {
    companion object {
        val AVAILABLE_MODELS = listOf(
            AIModel(AIProvider.ZAI, "glm-4.5-flash", maxContextWindow = 200000),
            AIModel(AIProvider.ZAI, "glm-4.6", maxContextWindow = 200000),
            AIModel(AIProvider.ZAI, "glm-4.7", maxContextWindow = 200000),
        )

        fun getModelById(id: String): AIModel? {
            return AVAILABLE_MODELS.find { it.displayName == id }
        }
    }
}
