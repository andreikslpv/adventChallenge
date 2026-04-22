package com.ai.adventchallenge.domain.context.strategy

import com.ai.adventchallenge.domain.context.ContextStrategy
import com.ai.adventchallenge.domain.model.Message

class FullHistoryStrategy : ContextStrategy {
    override fun onUserMessage(message: Message) {
    }

    override fun onAssistantMessage(message: Message) {
    }

    override suspend fun buildContext(allMessages: List<Message>): List<Message> {
        return allMessages
    }

    override fun reset() {
    }
}
