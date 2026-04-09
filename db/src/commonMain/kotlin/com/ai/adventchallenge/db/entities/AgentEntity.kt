package com.ai.adventchallenge.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "agents")
data class AgentEntity(
    @PrimaryKey
    val id: String,
    val systemPrompt: String,
    val temperature: Float,
    val providerName: String,
    val modelName: String,
    val maxTokens: String,
    val responseType: String,
    val stopWord: String
) {
    companion object {
        fun fromDomain(
            id: String,
            systemPrompt: String,
            temperature: Float,
            providerName: String,
            modelName: String,
            maxTokens: String,
            responseType: String,
            stopWord: String
        ): AgentEntity {
            return AgentEntity(
                id = id,
                systemPrompt = systemPrompt,
                temperature = temperature,
                providerName = providerName,
                modelName = modelName,
                maxTokens = maxTokens,
                responseType = responseType,
                stopWord = stopWord
            )
        }
    }
}
