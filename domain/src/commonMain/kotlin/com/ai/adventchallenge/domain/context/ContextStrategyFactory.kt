package com.ai.adventchallenge.domain.context

import com.ai.adventchallenge.domain.context.strategy.FullHistoryStrategy
import com.ai.adventchallenge.domain.context.strategy.SlidingWindowStrategy
import com.ai.adventchallenge.domain.context.strategy.StickyFactsStrategy
import com.ai.adventchallenge.domain.context.strategy.SummaryStrategy
import com.ai.adventchallenge.domain.service.FactsAgent
import com.ai.adventchallenge.domain.service.SummarizerAgent

class ContextStrategyFactory(
    private val summarizerAgent: SummarizerAgent,
    private val factsAgent: FactsAgent
) {
    fun create(settings: ContextSettings): ContextStrategy {
        return when (settings.strategy) {
            ContextStrategyType.FULL_HISTORY -> FullHistoryStrategy()
            ContextStrategyType.SLIDING_WINDOW -> SlidingWindowStrategy(settings)
            ContextStrategyType.STICKY_FACTS -> StickyFactsStrategy(settings, factsAgent)
            ContextStrategyType.SUMMARY -> SummaryStrategy(settings, summarizerAgent)
        }
    }
}
