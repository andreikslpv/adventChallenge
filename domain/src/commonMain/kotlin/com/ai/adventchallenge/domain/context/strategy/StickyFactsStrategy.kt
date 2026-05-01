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
            println("[StickyFacts] Updated facts: $currentFacts")
        }
    }

    override suspend fun onAssistantMessage(message: Message) {
    }

    override fun buildContext(allMessages: List<Message>): List<Message> {
        val factsText = buildFactsString()
        println("[StickyFacts] buildContext — factsText: $factsText")
        val windowSize = settings.factsWindowSize

        val baseSystem = allMessages.lastOrNull { it.role == Role.SYSTEM }
        val nonSystemMessages = allMessages.filter { it.role != Role.SYSTEM }

        val recentMessages = if (nonSystemMessages.size <= windowSize) {
            nonSystemMessages
        } else {
            nonSystemMessages.takeLast(windowSize)
        }

        val combinedSystemContent = buildString {
            baseSystem?.content?.let {
                append(it)
                append("\n\n")
            }
            append("Known facts:\n")
            append(factsText)
        }

        val systemMessage = Message(
            role = Role.SYSTEM,
            content = combinedSystemContent
        )

        return listOf(systemMessage) + recentMessages
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
        if (state.isNullOrBlank()) return

        try {
            val parsed = json.decodeFromString<StickyFactsState>(state)
            currentFacts = parsed.facts
            lastProcessedMessageId = parsed.lastProcessedMessageId
            println("[StickyFacts] Restored facts: $currentFacts")
        } catch (e: Exception) {
            println("[StickyFacts] Failed to restore state: ${e.message}")
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
