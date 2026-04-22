package com.ai.adventchallenge.domain.context

import com.ai.adventchallenge.domain.model.Message

interface ContextStrategy {
    fun onUserMessage(message: Message)
    fun onAssistantMessage(message: Message)

    suspend fun buildContext(allMessages: List<Message>): List<Message>

    fun reset()
}
