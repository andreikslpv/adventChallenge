package com.ai.adventchallenge.domain.context.strategy

import com.ai.adventchallenge.domain.context.ContextSettings
import com.ai.adventchallenge.domain.context.ContextStrategy
import com.ai.adventchallenge.domain.model.Message
import com.ai.adventchallenge.domain.service.SummarizerAgent
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class SummaryStrategy(
    private val settings: ContextSettings,
    private val summarizerAgent: SummarizerAgent
) : ContextStrategy {
    private var currentSummary = ""
    private var summarizedMessageCount = 0
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
        val recentMessages = if (summarizedMessageCount > 0) {
            allMessages.drop(summarizedMessageCount)
        } else {
            allMessages
        }

        return if (currentSummary.isNotEmpty()) {
            val summaryMessage = Message(
                role = "system",
                content = "Previous conversation summary:\n$currentSummary"
            )
            listOf(summaryMessage) + recentMessages
        } else {
            recentMessages
        }
    }

    override fun reset() {
        currentSummary = ""
        summarizedMessageCount = 0
        pendingMessages.clear()
    }

    override fun serializeState(): String {
        val state = SummaryState(
            currentSummary = currentSummary,
            summarizedMessageCount = summarizedMessageCount
        )
        return json.encodeToString(state)
    }

    override fun restoreState(state: String?) {
        if (state != null) {
            try {
                val parsed = json.decodeFromString<SummaryState>(state)
                currentSummary = parsed.currentSummary
                summarizedMessageCount = parsed.summarizedMessageCount
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
                    summarizedMessageCount += pendingMessages.size
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
        val summarizedMessageCount: Int = 0
    )

    companion object {
        private val json = Json {
            ignoreUnknownKeys = true
            isLenient = true
        }
    }
}
