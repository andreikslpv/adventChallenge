package com.ai.adventchallenge.domain.model

import kotlinx.serialization.Serializable
import kotlin.time.Clock

@Serializable
data class Message(
    val id: String = "",
    val sessionId: String = "",
    val parentId: String? = null,
    val role: Role,
    val content: String,
    val timestamp: Long = Clock.System.now().toEpochMilliseconds(),
    val systemPrompt: String = "",
    val agentId: String = "",
    val characterCount: Int = 0,
    val tokenCount: Int = 0,
    val outgoingTokenCount: Int = 0
)
