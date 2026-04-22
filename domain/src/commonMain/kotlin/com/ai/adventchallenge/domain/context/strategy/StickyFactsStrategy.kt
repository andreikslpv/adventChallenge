package com.ai.adventchallenge.domain.context.strategy

import com.ai.adventchallenge.domain.context.ContextSettings
import com.ai.adventchallenge.domain.context.ContextStrategy
import com.ai.adventchallenge.domain.context.Facts
import com.ai.adventchallenge.domain.context.FactsExtractor
import com.ai.adventchallenge.domain.model.Message

class StickyFactsStrategy(
    private val settings: ContextSettings,
    private val factsExtractor: FactsExtractor
) : ContextStrategy {
    private var currentFacts = Facts()

    override fun onUserMessage(message: Message) {
    }

    override fun onAssistantMessage(message: Message) {
    }

    override suspend fun buildContext(allMessages: List<Message>): List<Message> {
        val lastUserMessage = allMessages.lastOrNull { it.role == "user" }
        
        if (lastUserMessage != null) {
            currentFacts = factsExtractor.updateFacts(currentFacts, lastUserMessage.content)
        }

        val factsText = buildFactsString()
        val windowSize = settings.factsWindowSize

        val messagesWithoutSummaries = allMessages.filterNot { it.isSummarized }
        val recentMessages = if (messagesWithoutSummaries.size <= windowSize) {
            messagesWithoutSummaries
        } else {
            messagesWithoutSummaries.takeLast(windowSize)
        }

        val systemMessage = Message(
            role = "system",
            content = "Known facts:\n$factsText"
        )

        return listOf(systemMessage) + recentMessages
    }

    override fun reset() {
        currentFacts = Facts()
    }

    private fun buildFactsString(): String {
        return buildString {
            currentFacts.goal?.let { append("Goal: $it\n") }
            currentFacts.constraints?.let { append("Constraints: $it\n") }
            currentFacts.preferences?.let { append("Preferences: $it\n") }
            currentFacts.decisions?.let { append("Decisions: $it\n") }
            currentFacts.requirements?.let { append("Requirements: $it\n") }
            currentFacts.other?.let { append("Other: $it\n") }
        }.ifEmpty { "No facts recorded yet." }
    }
}
