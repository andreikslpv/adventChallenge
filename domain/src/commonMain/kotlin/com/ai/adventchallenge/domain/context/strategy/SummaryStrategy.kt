package com.ai.adventchallenge.domain.context.strategy

import com.ai.adventchallenge.domain.context.ContextSettings
import com.ai.adventchallenge.domain.context.ContextStrategy
import com.ai.adventchallenge.domain.model.Message
import com.ai.adventchallenge.domain.service.SummarizerAgent

class SummaryStrategy(
    private val settings: ContextSettings,
    private val summarizerAgent: SummarizerAgent
) : ContextStrategy {
    private var currentSummary = ""

    override fun onUserMessage(message: Message) {
    }

    override fun onAssistantMessage(message: Message) {
    }

    override suspend fun buildContext(allMessages: List<Message>): List<Message> {
        val triggerSize = settings.summaryTriggerSize
        val unsummarizedMessages = allMessages.filterNot { it.isSummarized }

        if (unsummarizedMessages.size >= triggerSize) {
            val result = summarizerAgent.summarize(currentSummary, unsummarizedMessages)
            result.fold(
                onSuccess = { newSummary ->
                    currentSummary = newSummary
                },
                onFailure = {
                }
            )
        }

        return buildContextWithSummary(allMessages)
    }

    override fun reset() {
        currentSummary = ""
    }

    private fun buildContextWithSummary(allMessages: List<Message>): List<Message> {
        val unsummarizedMessages = allMessages.filterNot { it.isSummarized }
        
        return if (currentSummary.isNotEmpty()) {
            val summaryMessage = Message(
                role = "system",
                content = "Previous conversation summary:\n$currentSummary"
            )
            listOf(summaryMessage) + unsummarizedMessages
        } else {
            unsummarizedMessages
        }
    }
}
