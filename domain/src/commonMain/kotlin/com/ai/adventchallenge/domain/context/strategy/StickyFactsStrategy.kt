package com.ai.adventchallenge.domain.context.strategy

import com.ai.adventchallenge.domain.context.ContextSettings
import com.ai.adventchallenge.domain.context.ContextStrategy
import com.ai.adventchallenge.domain.context.Facts
import com.ai.adventchallenge.domain.context.FactsExtractor
import com.ai.adventchallenge.domain.model.Message
import com.ai.adventchallenge.domain.model.Role
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class StickyFactsStrategy(
    private val settings: ContextSettings,
    private val factsExtractor: FactsExtractor
) : ContextStrategy {
    private var currentFacts = Facts()
    private var lastProcessedMessageId = ""

    override suspend fun onUserMessage(message: Message) {
        if (message.id != lastProcessedMessageId) {
            currentFacts = factsExtractor.updateFacts(currentFacts, message.content)
            lastProcessedMessageId = message.id
        }
    }

    override suspend fun onAssistantMessage(message: Message) {
    }

    override fun buildContext(allMessages: List<Message>): List<Message> {
        val factsText = buildFactsString()
        val windowSize = settings.factsWindowSize

        val nonSystemMessages = allMessages.filter { it.role != Role.SYSTEM }

        val recentMessages = if (nonSystemMessages.size <= windowSize) {
            nonSystemMessages
        } else {
            nonSystemMessages.takeLast(windowSize)
        }

        val factsSystemMessage = Message(
            role = Role.SYSTEM,
            content = "Known facts:\n$factsText"
        )

        return listOf(factsSystemMessage) + recentMessages
    }

    override fun reset() {
        currentFacts = Facts()
        lastProcessedMessageId = ""
    }

    override fun serializeState(): String {
        val state = StickyFactsState(
            facts = currentFacts,
            lastProcessedMessageId = lastProcessedMessageId
        )
        return json.encodeToString(state)
    }

    override fun restoreState(state: String?) {
        if (state != null) {
            try {
                val parsed = json.decodeFromString<StickyFactsState>(state)
                currentFacts = parsed.facts
                lastProcessedMessageId = parsed.lastProcessedMessageId
            } catch (_: Exception) {
            }
        }
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

    @Serializable
    data class StickyFactsState(
        val facts: Facts = Facts(),
        val lastProcessedMessageId: String = ""
    )

    companion object {
        private val json = Json {
            ignoreUnknownKeys = true
            isLenient = true
        }
    }
}
