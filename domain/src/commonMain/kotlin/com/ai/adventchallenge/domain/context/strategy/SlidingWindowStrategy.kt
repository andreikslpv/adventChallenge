package com.ai.adventchallenge.domain.context.strategy

import com.ai.adventchallenge.domain.context.ContextSettings
import com.ai.adventchallenge.domain.context.ContextStrategy
import com.ai.adventchallenge.domain.model.Message
import com.ai.adventchallenge.domain.model.Role

class SlidingWindowStrategy(
    private val settings: ContextSettings
) : ContextStrategy {
    override suspend fun onUserMessage(message: Message) {
    }

    override suspend fun onAssistantMessage(message: Message) {
    }

    override fun buildContext(allMessages: List<Message>): List<Message> {
        val nonSystemMessages = allMessages.filter { it.role != Role.SYSTEM }

        val windowed = if (nonSystemMessages.size <= settings.slidingWindowSize) {
            nonSystemMessages
        } else {
            nonSystemMessages.takeLast(settings.slidingWindowSize)
        }

        val windowIds = windowed.map { it.id }.toSet()

        return allMessages.filter {
            it.role == Role.SYSTEM || it.id in windowIds
        }
    }

    override fun reset() {
    }

    override fun serializeState(): String? = null

    override fun restoreState(state: String?) {
    }
}
