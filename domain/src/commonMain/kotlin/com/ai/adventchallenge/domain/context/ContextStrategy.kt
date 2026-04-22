package com.ai.adventchallenge.domain.context

import com.ai.adventchallenge.domain.model.Message

interface ContextStrategy {
    suspend fun onUserMessage(message: Message)
    suspend fun onAssistantMessage(message: Message)

    fun buildContext(allMessages: List<Message>): List<Message>

    fun reset()

    fun serializeState(): String?
    fun restoreState(state: String?)
}
