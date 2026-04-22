package com.ai.adventchallenge.domain.context

import kotlinx.serialization.Serializable

@Serializable
data class ContextSettings(
    val strategy: ContextStrategyType,

    val slidingWindowSize: Int = 10,

    val factsWindowSize: Int = 6,

    val summaryTriggerSize: Int = 20
)
