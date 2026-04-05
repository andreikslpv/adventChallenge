package com.ai.adventchallenge.agent

import com.ai.adventchallenge.api.AIModel
import com.ai.adventchallenge.api.ZaiApiService
import com.ai.adventchallenge.api.dtos.ChatMessage

data class AgentSettings(
    val systemPrompt: String = "",
    val temperature: Float = 0.7f,
    val selectedModel: AIModel = AIModel.AVAILABLE_MODELS[1],
    val maxTokens: String = "",
    val responseType: String = "text",
    val stopWord: String = ""
)

sealed class AgentResponse {
    data class Success(val message: ChatMessage, val tokenCount: Int) : AgentResponse()
    data class Error(val message: String) : AgentResponse()
}

data class Agent(
    val id: String,
    val settings: AgentSettings = AgentSettings()
) {
    suspend fun processRequest(
        userMessage: String,
        conversationHistory: List<ChatMessage> = emptyList(),
        apiService: ZaiApiService
    ): AgentResponse {
        return try {
            val messagesToSend = buildMessageList(userMessage, conversationHistory)
            
            val maxTokens = settings.maxTokens.toIntOrNull()
            val responseType = if (settings.responseType == "json") "json_object" else "text"
            val stopWord = settings.stopWord.takeIf { it.isNotBlank() }

            val result = apiService.sendMessage(
                messages = messagesToSend,
                temperature = settings.temperature,
                maxTokens = maxTokens,
                responseType = responseType,
                stopWord = stopWord,
                model = settings.selectedModel
            )

            result.fold(
                onSuccess = { (message, tokenCount) ->
                    AgentResponse.Success(message, tokenCount)
                },
                onFailure = { exception ->
                    AgentResponse.Error(exception.message ?: "Unknown error")
                }
            )
        } catch (e: Exception) {
            AgentResponse.Error("Processing failed: ${e.message}")
        }
    }

    fun buildMessageList(
        userMessage: String,
        conversationHistory: List<ChatMessage>
    ): List<ChatMessage> {
        val messages = mutableListOf<ChatMessage>()

        if (settings.systemPrompt.isNotBlank()) {
            messages.add(
                ChatMessage(
                    role = "system",
                    content = settings.systemPrompt
                )
            )
        }

        messages.addAll(conversationHistory)
        messages.add(ChatMessage(role = "user", content = userMessage))

        return messages
    }
}
