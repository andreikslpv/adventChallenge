package com.ai.adventchallenge.data.api

import com.ai.adventchallenge.config.BuildKonfig
import io.ktor.client.HttpClient
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import kotlinx.io.IOException
import kotlinx.serialization.json.Json

class ZaiApiService(
    private val client: HttpClient
) {
    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = false
        isLenient = true
        encodeDefaults = true
    }

    suspend fun sendMessage(
        messages: List<ChatMessage>,
        temperature: Float = 0.7f,
        maxTokens: Int? = null,
        responseType: String = "text",
        stopWord: String? = null,
        model: AIModel = AIModel.AVAILABLE_MODELS[1]
    ): Result<Pair<ChatMessage, Int>> {
        return try {
            val requestBuilder = ChatRequest(
                messages = messages,
                temperature = temperature,
                model = model.modelName
            )

            val requestBody = if (maxTokens != null || responseType != "text" || stopWord != null) {
                requestBuilder.copy(
                    maxTokens = maxTokens,
                    responseFormat = ResponseFormat(type = responseType),
                    stop = stopWord?.let { listOf(it) } ?: emptyList()
                )
            } else {
                requestBuilder
            }

            val apiKey = when (model.provider) {
                AIProvider.ZAI -> BuildKonfig.ZAI_API_KEY
                AIProvider.OPENAI -> BuildKonfig.OPENAI_API_KEY
            }

            val response = client.post(model.provider.baseUrl) {
                when (model.provider.apiKeyHeaderName) {
                    "Authorization" -> bearerAuth(apiKey)
                    else -> header(model.provider.apiKeyHeaderName, apiKey)
                }
                setBody(requestBody)
            }

            val responseBody = response.bodyAsText()

            if (response.status.value !in 200..299) {
                val errorMessage = try {
                    val errorResponse = json.decodeFromString<ErrorResponse>(responseBody)
                    errorResponse.error?.message
                        ?: "API error: ${response.status.value} ${response.status.description}"
                } catch (_: Exception) {
                    "API error: ${response.status.value} ${response.status.description}"
                }
                return Result.failure(IOException(errorMessage))
            }

            if (responseBody.isEmpty()) {
                return Result.failure(IOException("Empty response body"))
            }

            val chatResponse = json.decodeFromString<ChatResponse>(responseBody)
            chatResponse.error?.let { error ->
                return Result.failure(IOException(error.message ?: "API error"))
            }

            if (chatResponse.choices.isNullOrEmpty()) {
                return Result.failure(IOException("No choices in response: $responseBody"))
            }

            val message = chatResponse.choices[0].message
            val tokenCount = chatResponse.usage?.completionTokens ?: 0

            Result.success(Pair(message, tokenCount))
        } catch (e: Exception) {
            Result.failure(IOException("Unexpected error: ${e.message}"))
        }
    }
}
