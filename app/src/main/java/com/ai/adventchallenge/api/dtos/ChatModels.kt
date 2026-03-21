package com.ai.adventchallenge.api.dtos

import kotlinx.serialization.Serializable

@Serializable
data class ChatMessage(
    val role: String,
    val content: String
)

@Serializable
data class Thinking(
    val type: String = "disabled"
)

@Serializable
data class ChatRequest(
    val messages: List<ChatMessage>,
    val model: String = "glm-4.7-flashx",
    val temperature: Float = 0.7f,
    val thinking: Thinking = Thinking()
)

@Serializable
data class ChatResponse(
    val choices: List<Choice>? = null,
    val error: ErrorDetail? = null
)

@Serializable
data class Choice(
    val message: ChatMessage
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
