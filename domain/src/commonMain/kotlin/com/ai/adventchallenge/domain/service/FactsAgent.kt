package com.ai.adventchallenge.domain.service

import com.ai.adventchallenge.domain.context.Facts
import com.ai.adventchallenge.domain.context.FactsExtractor
import com.ai.adventchallenge.domain.model.AIModel
import com.ai.adventchallenge.domain.model.Message
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.put

class FactsAgent(
    private val aiService: AIService
) : FactsExtractor {
    companion object {
        private const val SYSTEM_PROMPT = "You are an AI agent responsible for maintaining structured memory of a conversation.\n" +
            "Your task is to update a JSON object called \"facts\" based on a new user message.\n" +
            "Rules:\n" +
            "- Extract only important and stable information:\n" +
            "  - goal\n" +
            "  - constraints\n" +
            "  - preferences\n" +
            "  - decisions\n" +
            "  - requirements\n" +
            "- If the user changes something previously stated:\n" +
            "  - update the corresponding field\n" +
            "  - remove outdated information\n" +
            "- Do not duplicate data\n" +
            "- Keep the output concise and structured\n" +
            "- Do not invent information\n" +
            "- If a field has no relevant data, keep it null\n" +
            "Always return valid JSON."

        private const val USER_PROMPT_TEMPLATE = "Current facts:\n{FACTS_JSON}\n\n" +
            "New user message:\n{USER_MESSAGE}\n\n" +
            "Update the facts JSON.\n\n" +
            "Return ONLY JSON in this format:\n\n" +
            "{\n" +
            "  \"goal\": \"...\",\n" +
            "  \"constraints\": \"...\",\n" +
            "  \"preferences\": \"...\",\n" +
            "  \"decisions\": \"...\",\n" +
            "  \"requirements\": \"...\",\n" +
            "  \"other\": \"...\"\n" +
            "}"

        private val FACTS_MODEL = AIModel.AVAILABLE_MODELS[1]
        
        private val json = Json {
            ignoreUnknownKeys = true
            isLenient = true
        }
    }

    override suspend fun updateFacts(
        currentFacts: Facts,
        newMessage: String
    ): Facts {
        return try {
            val factsJson = buildJsonObject {
                put("goal", currentFacts.goal)
                put("constraints", currentFacts.constraints)
                put("preferences", currentFacts.preferences)
                put("decisions", currentFacts.decisions)
                put("requirements", currentFacts.requirements)
                put("other", currentFacts.other)
            }

            val userPrompt = USER_PROMPT_TEMPLATE
                .replace("{FACTS_JSON}", factsJson.toString())
                .replace("{USER_MESSAGE}", newMessage)

            val messagesToSend = listOf(
                Message(role = "system", content = SYSTEM_PROMPT),
                Message(role = "user", content = userPrompt)
            )

            val result = aiService.sendMessage(
                messages = messagesToSend,
                temperature = 0.3f,
                maxTokens = 500,
                responseType = "text",
                stopWord = null,
                model = FACTS_MODEL
            )

            result.fold(
                onSuccess = { (message, _) ->
                    parseFactsFromResponse(message.content)
                },
                onFailure = { _ -> currentFacts }
            )
        } catch (_: Exception) {
            currentFacts
        }
    }

    private fun parseFactsFromResponse(response: String): Facts {
        return try {
            val jsonText = extractJsonFromResponse(response)
            val jsonObject = json.parseToJsonElement(jsonText).jsonObject
            json.decodeFromJsonElement<FactUpdate>(jsonObject).toFacts()
        } catch (_: Exception) {
            Facts()
        }
    }

    private fun extractJsonFromResponse(response: String): String {
        val trimmed = response.trim()
        val firstBrace = trimmed.indexOf('{')
        val lastBrace = trimmed.lastIndexOf('}')
        
        return if (firstBrace != -1 && lastBrace != -1 && lastBrace > firstBrace) {
            trimmed.substring(firstBrace, lastBrace + 1)
        } else {
            trimmed
        }
    }

    @kotlinx.serialization.Serializable
    private data class FactUpdate(
        val goal: String? = null,
        val constraints: String? = null,
        val preferences: String? = null,
        val decisions: String? = null,
        val requirements: String? = null,
        val other: String? = null
    ) {
        fun toFacts() = Facts(
            goal = goal?.takeIf { it.isNotBlank() },
            constraints = constraints?.takeIf { it.isNotBlank() },
            preferences = preferences?.takeIf { it.isNotBlank() },
            decisions = decisions?.takeIf { it.isNotBlank() },
            requirements = requirements?.takeIf { it.isNotBlank() },
            other = other?.takeIf { it.isNotBlank() }
        )
    }
}
