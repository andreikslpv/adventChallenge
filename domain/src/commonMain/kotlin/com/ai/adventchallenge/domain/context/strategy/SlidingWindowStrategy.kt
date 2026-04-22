package com.ai.adventchallenge.domain.context.strategy

import com.ai.adventchallenge.domain.context.ContextSettings
import com.ai.adventchallenge.domain.context.ContextStrategy
import com.ai.adventchallenge.domain.model.Message

class SlidingWindowStrategy(
    private val settings: ContextSettings
) : ContextStrategy {
    override fun onUserMessage(message: Message) {
    }

    override fun onAssistantMessage(message: Message) {
    }

    override suspend fun buildContext(allMessages: List<Message>): List<Message> {
        val windowSize = settings.slidingWindowSize
        return if (allMessages.size <= windowSize) {
            allMessages
        } else {
            allMessages.takeLast(windowSize)
        }
    }

    override fun reset() {
    }
}
