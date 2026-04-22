package com.ai.adventchallenge.domain.service

import com.ai.adventchallenge.domain.model.AIModel
import com.ai.adventchallenge.domain.model.Message
import com.ai.adventchallenge.domain.model.Role

class SummarizerAgent(
    private val aiService: AIService
) {
    companion object {
        private const val SYSTEM_PROMPT = "You are a context summarization agent.\n" +
            "Your task is to maintain a compact, accurate, and up-to-date summary of a conversation.\n" +
            "Rules:\n" +
            "- You MUST compress conversation history into a concise summary.\n" +
            "- You MUST preserve important facts, user goals, constraints, and ongoing tasks.\n" +
            "- You MUST remove irrelevant details, repetitions, and small talk.\n" +
            "- You MUST NOT invent information.\n" +
            "- You MUST NOT include instructions or meta commentary.\n" +
            "- You MUST write in a neutral, factual style.\n" +
            "Language:\n" +
            "The summary must be written in the same language as the messages.\n" +
            "IMPORTANT:\n" +
            "- The summary is used as long-term memory for another AI agent.\n" +
            "- It must be self-contained and understandable without original messages.\n" +
            "- It must be updated incrementally using previous summary + new messages.\n" +
            "- Keep the summary under 1500 characters.\n" +
            "Focus on extracting:\n" +
            "- User intent and goals\n" +
            "- Key entities (projects, technologies, objects)\n" +
            "- Decisions made\n" +
            "- Current state of work\n" +
            "- Open problems\n" +
            "Output format:\n" +
            "Plain text summary only. No markdown. No explanations.\n" +
            "Keep the summary under 1500 characters."

        private const val USER_PROMPT_TEMPLATE = "Update the conversation summary.\n\n" +
            "Previous summary:\n{OLD_SUMMARY}\n\n" +
            "New messages:\n{MESSAGES_BLOCK}\n\n" +
            "Instructions:\n" +
            "- Merge previous summary with new messages\n" +
            "- Keep it concise but informative\n" +
            "- Preserve important details\n" +
            "- Remove redundancy\n" +
            "- Maintain continuity\n\n" +
            "The summary must be written in the same language as the messages.\n\n" +
            "Return updated summary only."

        private val SUMMARIZER_MODEL = AIModel.AVAILABLE_MODELS[1]
    }

    suspend fun summarize(
        previousSummary: String,
        messages: List<Message>
    ): Result<String> {
        if (messages.isEmpty()) {
            return Result.success(previousSummary)
        }

        return try {
            val messagesText = messages.joinToString("\n\n") { message ->
                "${message.role}: ${message.content}"
            }

            val userPrompt = USER_PROMPT_TEMPLATE
                .replace("{OLD_SUMMARY}", previousSummary.ifEmpty { "None" })
                .replace("{MESSAGES_BLOCK}", messagesText)

            val messagesToSend = listOf(
                Message(role = Role.SYSTEM, content = SYSTEM_PROMPT),
                Message(role = Role.USER, content = userPrompt)
            )

            val result = aiService.sendMessage(
                messages = messagesToSend,
                temperature = 0.3f,
                maxTokens = 1000,
                responseType = "text",
                stopWord = null,
                model = SUMMARIZER_MODEL
            )

            result.map { (message, _) -> message.content }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
