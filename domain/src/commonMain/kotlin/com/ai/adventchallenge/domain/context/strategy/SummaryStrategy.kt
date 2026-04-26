package com.ai.adventchallenge.domain.context.strategy

import com.ai.adventchallenge.domain.context.ContextSettings
import com.ai.adventchallenge.domain.context.ContextStrategy
import com.ai.adventchallenge.domain.model.Message
import com.ai.adventchallenge.domain.model.Role
import com.ai.adventchallenge.domain.service.SummarizerAgent
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class SummaryStrategy(
    private val settings: ContextSettings,
    private val summarizerAgent: SummarizerAgent
) : ContextStrategy {
    private var currentSummary = ""
    private var lastSummarizedMessageId: String? = null
    private val pendingMessages = mutableListOf<Message>()

    override suspend fun onUserMessage(message: Message) {
        pendingMessages.add(message)
        trySummarize()
    }

    override suspend fun onAssistantMessage(message: Message) {
        pendingMessages.add(message)
        trySummarize()
    }

    override fun buildContext(allMessages: List<Message>): List<Message> {
        val recentMessages = if (lastSummarizedMessageId != null) {
            val index = allMessages.indexOfFirst { it.id == lastSummarizedMessageId }
            if (index != -1) allMessages.drop(index + 1) else allMessages
        } else {
            allMessages
        }

        val nonSystemMessages = recentMessages.filter { it.role != Role.SYSTEM }

        return if (currentSummary.isNotEmpty()) {
            val summaryMessage = Message(
                role = Role.SYSTEM,
                content = "Previous conversation summary:\n$currentSummary"
            )
            listOf(summaryMessage) + nonSystemMessages
        } else {
            nonSystemMessages
        }
    }

    override fun reset() {
        currentSummary = ""
        lastSummarizedMessageId = null
        pendingMessages.clear()
    }

    override fun serializeState(): String {
        val state = SummaryState(
            currentSummary = currentSummary,
            lastSummarizedMessageId = lastSummarizedMessageId
        )
        return json.encodeToString(state)
    }

    override fun restoreState(state: String?) {
        if (state != null) {
            try {
                val parsed = json.decodeFromString<SummaryState>(state)
                currentSummary = parsed.currentSummary
                lastSummarizedMessageId = parsed.lastSummarizedMessageId
                pendingMessages.clear()
            } catch (_: Exception) {
            }
        }
    }

    private suspend fun trySummarize() {
        if (pendingMessages.size >= settings.summaryTriggerSize) {
            val result = summarizerAgent.summarize(currentSummary, pendingMessages.toList())
            result.fold(
                onSuccess = { newSummary ->
                    currentSummary = newSummary
                    lastSummarizedMessageId = pendingMessages.last().id
                    pendingMessages.clear()
                },
                onFailure = {
                }
            )
        }
    }

    @Serializable
    data class SummaryState(
        val currentSummary: String = "",
        val lastSummarizedMessageId: String? = null
    )

    companion object {
        private val json = Json {
            ignoreUnknownKeys = true
            isLenient = true
        }
    }
}
