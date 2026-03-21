package com.ai.adventchallenge.api

import com.ai.adventchallenge.api.dtos.ChatMessage
import com.ai.adventchallenge.api.dtos.ChatRequest
import com.ai.adventchallenge.api.dtos.ChatResponse
import com.ai.adventchallenge.api.dtos.Choice
import com.ai.adventchallenge.api.dtos.ErrorResponse
import com.ai.adventchallenge.api.dtos.ErrorDetail
import com.ai.adventchallenge.BuildConfig
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

class ZaiApiService(
    private val client: OkHttpClient
) {
    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = false
        isLenient = true
        encodeDefaults = true
    }

    private val mediaType = "application/json".toMediaType()

    suspend fun sendMessage(messages: List<ChatMessage>, temperature: Float = 0.7f): Result<ChatMessage> {
        return withContext(Dispatchers.IO) {
            try {
                val requestBody = ChatRequest(messages = messages, temperature = temperature)
                val jsonBody = json.encodeToString(requestBody)

                val request = Request.Builder()
                    .url("https://api.z.ai/api/paas/v4/chat/completions")
                    .addHeader("Authorization", "Bearer ${BuildConfig.API_KEY}")
                    .addHeader("Content-Type", "application/json")
                    .post(jsonBody.toRequestBody(mediaType))
                    .build()

                val response = client.newCall(request).execute()

                if (!response.isSuccessful) {
                    val errorBody = response.body?.string()
                    val errorMessage = try {
                        val errorResponse = json.decodeFromString<ErrorResponse>(errorBody ?: "")
                        errorResponse.error?.message ?: "API error: ${response.code} ${response.message}"
                    } catch (_: Exception) {
                        "API error: ${response.code} ${response.message}"
                    }
                    return@withContext Result.failure(IOException(errorMessage))
                }

                val responseBody = response.body?.string()
                if (responseBody.isNullOrEmpty()) {
                    return@withContext Result.failure(IOException("Empty response body"))
                }

                val chatResponse = json.decodeFromString<ChatResponse>(responseBody)
                chatResponse.error?.let { error ->
                    return@withContext Result.failure(IOException(error.message ?: "API error"))
                }
                
                if (chatResponse.choices.isNullOrEmpty()) {
                    return@withContext Result.failure(IOException("No choices in response: $responseBody"))
                }

                Result.success(chatResponse.choices[0].message)
            } catch (e: Exception) {
                Result.failure(IOException("Unexpected error: ${e.message}"))
            }
        }
    }
}
