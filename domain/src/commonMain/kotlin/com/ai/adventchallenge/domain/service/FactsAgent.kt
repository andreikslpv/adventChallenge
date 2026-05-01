package com.ai.adventchallenge.domain.service

import com.ai.adventchallenge.domain.context.Facts
import com.ai.adventchallenge.domain.context.FactsExtractor
import com.ai.adventchallenge.domain.model.AIModel
import com.ai.adventchallenge.domain.model.Message
import com.ai.adventchallenge.domain.model.Role
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
            "Your task is to update a JSON object called \"facts\" based on a new user message.\n\n" +
            "STRICT RULES:\n\n" +
            "1. Output MUST be valid raw JSON only\n" +
            "   - Do NOT wrap in markdown\n" +
            "   - Do NOT use ```json or ```\n" +
            "   - Do NOT add explanations\n\n" +
            "2. Preserve existing facts unless explicitly changed\n" +
            "   - If the new message refines a goal -> UPDATE it\n" +
            "   - If it is unrelated -> KEEP previous value\n\n" +
            "3. Extract only stable information:\n" +
            "   - goal\n" +
            "   - constraints\n" +
            "   - preferences\n" +
            "   - decisions\n" +
            "   - requirements\n\n" +
            "4. Never invent data\n\n" +
            "5. If no new info for a field -> keep previous value\n\n" +
            "6. If user contradicts previous info -> replace it"

        private const val USER_PROMPT_TEMPLATE = "Current facts:\n{FACTS_JSON}\n\n" +
            "New user message:\n{USER_MESSAGE}\n\n" +
            "Update the facts JSON. Return EXACTLY this structure with updated values:\n\n" +
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
                Message(role = Role.SYSTEM, content = SYSTEM_PROMPT),
                Message(role = Role.USER, content = userPrompt)
            )

            println("[FactsAgent] Extracting facts from message: ${newMessage.take(80)}")

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
                    val updated = parseFactsFromResponse(message.content, currentFacts)
                    println("[FactsAgent] Updated facts: $updated")
                    updated
                },
                onFailure = { e ->
                    println("[FactsAgent] Extraction failed: ${e.message}")
                    currentFacts
                }
            )
        } catch (e: Exception) {
            println("[FactsAgent] Extraction error: ${e.message}")
            currentFacts
        }
    }

    private fun parseFactsFromResponse(response: String, currentFacts: Facts): Facts {
        return try {
            val jsonText = extractJsonFromResponse(response)
            val jsonObject = json.parseToJsonElement(jsonText).jsonObject
            val update = json.decodeFromJsonElement<FactUpdate>(jsonObject)
            update.mergeWith(currentFacts)
        } catch (e: Exception) {
            println("[FactsAgent] Parse error: ${e.message}")
            currentFacts
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
        private fun String?.coalesce(previous: String?): String? =
            this?.takeIf { it.isNotBlank() } ?: previous

        fun mergeWith(current: Facts): Facts = Facts(
            goal = goal.coalesce(current.goal),
            constraints = constraints.coalesce(current.constraints),
            preferences = preferences.coalesce(current.preferences),
            decisions = decisions.coalesce(current.decisions),
            requirements = requirements.coalesce(current.requirements),
            other = other.coalesce(current.other)
        )
    }
}
