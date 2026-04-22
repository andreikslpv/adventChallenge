package com.ai.adventchallenge.domain.context.strategy

import com.ai.adventchallenge.domain.context.ContextSettings
import com.ai.adventchallenge.domain.context.ContextStrategy
import com.ai.adventchallenge.domain.model.Message

class SlidingWindowStrategy(
    private val settings: ContextSettings
) : ContextStrategy {
    override suspend fun onUserMessage(message: Message) {
    }

    override suspend fun onAssistantMessage(message: Message) {
    }

    override fun buildContext(allMessages: List<Message>): List<Message> {
        val systemMessages = allMessages.filter { it.role == "system" }
        val nonSystemMessages = allMessages.filter { it.role != "system" }

        val windowed = if (nonSystemMessages.size <= settings.slidingWindowSize) {
            nonSystemMessages
        } else {
            nonSystemMessages.takeLast(settings.slidingWindowSize)
        }

        return systemMessages + windowed
    }

    override fun reset() {
    }

    override fun serializeState(): String? = null

    override fun restoreState(state: String?) {
    }
}
