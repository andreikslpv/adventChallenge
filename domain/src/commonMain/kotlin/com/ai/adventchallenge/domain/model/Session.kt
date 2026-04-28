package com.ai.adventchallenge.domain.model

import com.ai.adventchallenge.domain.context.ContextSettings
import com.ai.adventchallenge.domain.context.ContextStrategyType
import kotlin.time.Clock

data class Session(
    val id: String,
    val name: String,
    val createdAt: Long = Clock.System.now().toEpochMilliseconds(),
    val selectedAgentId: String = "",
    val summary: String = "",
    val contextSettings: ContextSettings = ContextSettings(
        strategy = ContextStrategyType.FULL_HISTORY
    ),
    val strategyStateJson: String = "",
    val currentMessageId: String? = null
)
