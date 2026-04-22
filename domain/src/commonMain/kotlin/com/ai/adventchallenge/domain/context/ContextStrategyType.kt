package com.ai.adventchallenge.domain.context

import kotlinx.serialization.Serializable

@Serializable
enum class ContextStrategyType {
    FULL_HISTORY,
    SLIDING_WINDOW,
    STICKY_FACTS,
    SUMMARY
}
