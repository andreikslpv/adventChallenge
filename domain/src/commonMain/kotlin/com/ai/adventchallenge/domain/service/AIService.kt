package com.ai.adventchallenge.domain.service

import com.ai.adventchallenge.domain.model.AIModel
import com.ai.adventchallenge.domain.model.Message

interface AIService {
    suspend fun sendMessage(
        messages: List<Message>,
        temperature: Float = 0.7f,
        maxTokens: Int? = null,
        responseType: String = "text",
        stopWord: String? = null,
        model: AIModel = AIModel.AVAILABLE_MODELS[1]
    ): Result<Pair<Message, Int>>
}
