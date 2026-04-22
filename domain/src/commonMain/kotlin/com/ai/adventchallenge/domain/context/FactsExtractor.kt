package com.ai.adventchallenge.domain.context

interface FactsExtractor {
    suspend fun updateFacts(
        currentFacts: Facts,
        newMessage: String
    ): Facts
}
