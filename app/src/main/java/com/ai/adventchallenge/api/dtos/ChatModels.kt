package com.ai.adventchallenge.api.dtos

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ChatMessage(
    val role: String,
    val content: String,
    val characterCount: Int = 0,
    val tokenCount: Int = 0
)

@Serializable
data class Thinking(
    val type: String = "disabled"
)

@Serializable
data class ResponseFormat(
    val type: String = "text"
)

@Serializable
data class ChatRequest(
    val messages: List<ChatMessage>,
    val model: String = "glm-4.7-flashx",
    val temperature: Float = 0.7f,
    val thinking: Thinking = Thinking(),
    @SerialName("max_tokens")
    val maxTokens: Int? = null,
    @SerialName("response_format")
    val responseFormat: ResponseFormat? = null,
    val stop: List<String> = emptyList(),
)

@Serializable
data class Usage(
    @SerialName("completion_tokens")
    val completionTokens: Int = 0,
    @SerialName("prompt_tokens")
    val promptTokens: Int = 0,
    @SerialName("total_tokens")
    val totalTokens: Int = 0
)

@Serializable
data class ChatResponse(
    val choices: List<Choice>? = null,
    val error: ErrorDetail? = null,
    val usage: Usage? = null
)

@Serializable
data class Choice(
    val message: ChatMessage,
    @SerialName("finish_reason")
    val finishReason: String? = null
)

@Serializable
data class ErrorResponse(
    val error: ErrorDetail? = null
)

@Serializable
data class ErrorDetail(
    val message: String? = null,
    val type: String? = null
)
