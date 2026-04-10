package com.ai.adventchallenge.domain.model

data class AgentSettings(
    val systemPrompt: String = "",
    val temperature: Float = 0.7f,
    val selectedModel: AIModel = AIModel.AVAILABLE_MODELS[1],
    val maxTokens: String = "",
    val responseType: String = "text",
    val stopWord: String = ""
)

sealed class AgentResponse {
    data class Success(val message: Message, val tokenCount: Int) : AgentResponse()
    data class Error(val message: String) : AgentResponse()
}

data class Agent(
    val id: String,
    val settings: AgentSettings = AgentSettings()
)
