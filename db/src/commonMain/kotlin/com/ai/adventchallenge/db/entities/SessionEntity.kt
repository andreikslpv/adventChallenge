package com.ai.adventchallenge.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sessions")
data class SessionEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val createdAt: Long,
    val selectedAgentId: String,
    val summary: String = "",
    val isCompressionEnabled: Boolean = false
)