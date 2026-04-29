package com.ai.adventchallenge.data.service

import com.ai.adventchallenge.data.api.ZaiApiService
import com.ai.adventchallenge.domain.model.AIModel
import com.ai.adventchallenge.domain.model.Message
import com.ai.adventchallenge.domain.model.Role
import com.ai.adventchallenge.domain.service.AIService
import com.ai.adventchallenge.domain.model.AIProvider as DomainAIProvider
import com.ai.adventchallenge.data.api.AIProvider as DataAIProvider
import com.ai.adventchallenge.data.api.ChatMessage as DataChatMessage
import com.ai.adventchallenge.data.api.AIModel as DataAIModel

class AIServiceImpl(
    private val apiService: ZaiApiService
) : AIService {
    
    override suspend fun sendMessage(
        messages: List<Message>,
        temperature: Float,
        maxTokens: Int?,
        responseType: String,
        stopWord: String?,
        model: AIModel
    ): Result<Pair<Message, Int>> {
        val dataMessages = messages.map { it.toDataChatMessage() }
        val dataModel = model.toDataAIModel()
        
        return apiService.sendMessage(
            messages = dataMessages,
            temperature = temperature,
            maxTokens = maxTokens,
            responseType = responseType,
            stopWord = stopWord,
            model = dataModel
        ).map { (dataMessage, tokenCount) ->
            Pair(dataMessage.toDomainMessage(), tokenCount)
        }
    }
}

private fun Message.toDataChatMessage() = DataChatMessage(
    id = id,
    role = role.value(),
    content = content,
    timestamp = timestamp,
    systemPrompt = systemPrompt,
    agentId = agentId,
    characterCount = characterCount,
    tokenCount = tokenCount
)

private fun DataChatMessage.toDomainMessage() = Message(
    id = id,
    role = Role.entries.find { it.value() == role } ?: Role.USER,
    content = content,
    timestamp = timestamp,
    systemPrompt = systemPrompt,
    agentId = agentId,
    characterCount = characterCount,
    tokenCount = tokenCount
)

private fun AIModel.toDataAIModel() = DataAIModel(
    provider = when (provider) {
        DomainAIProvider.ZAI -> DataAIProvider.ZAI
        DomainAIProvider.OPENAI -> DataAIProvider.OPENAI
    },
    modelName = modelName,
    displayName = displayName
)
