package com.ai.adventchallenge.domain.context.strategy

import com.ai.adventchallenge.domain.context.ContextStrategy
import com.ai.adventchallenge.domain.model.Message

class FullHistoryStrategy : ContextStrategy {
    override suspend fun onUserMessage(message: Message) {
    }

    override suspend fun onAssistantMessage(message: Message) {
    }

    override fun buildContext(allMessages: List<Message>): List<Message> {
        return allMessages
    }

    override fun reset() {
    }

    override fun serializeState(): String? = null

    override fun restoreState(state: String?) {
    }
}
