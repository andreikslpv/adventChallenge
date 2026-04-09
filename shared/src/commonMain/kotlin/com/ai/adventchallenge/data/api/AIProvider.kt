package com.ai.adventchallenge.data.api

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
    val displayName: String = "${provider.displayName}: $modelName"
) {
    companion object {
        val AVAILABLE_MODELS = listOf(
            AIModel(AIProvider.ZAI, "glm-4.5-flash"),
            AIModel(AIProvider.ZAI, "glm-4.6"),
            AIModel(AIProvider.ZAI, "glm-4.7"),
//            AIModel(AIProvider.OPENAI, "gpt-4o"),
//            AIModel(AIProvider.OPENAI, "gpt-4o-mini"),
//            AIModel(AIProvider.OPENAI, "gpt-3.5-turbo")
        )

        fun getModelById(id: String): AIModel? {
            return AVAILABLE_MODELS.find { it.displayName == id }
        }
    }
}