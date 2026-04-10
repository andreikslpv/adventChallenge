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
)