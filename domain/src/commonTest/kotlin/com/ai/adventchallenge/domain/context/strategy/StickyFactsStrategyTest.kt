package com.ai.adventchallenge.domain.context.strategy

import com.ai.adventchallenge.domain.context.ContextSettings
import com.ai.adventchallenge.domain.context.ContextStrategyType
import com.ai.adventchallenge.domain.context.Facts
import com.ai.adventchallenge.domain.context.FactsExtractor
import com.ai.adventchallenge.domain.model.Message
import com.ai.adventchallenge.domain.model.Role
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StickyFactsStrategyTest {

    private val settings = ContextSettings(
        strategy = ContextStrategyType.STICKY_FACTS,
        factsWindowSize = 10
    )

    private fun createStrategy(extractor: FakeFactsExtractor = FakeFactsExtractor()): StickyFactsStrategy {
        return StickyFactsStrategy(settings, extractor)
    }

    @Test
    fun factsPersistBetweenCalls() = runTest {
        val extractor = FakeFactsExtractor()
        val strategy1 = createStrategy(extractor)

        extractor.nextFacts = Facts(goal = "build a fitness app")
        val msg1 = Message(id = "msg-1", role = Role.USER, content = "My goal is to build a fitness app")
        strategy1.onUserMessage(msg1)

        val stateJson = strategy1.serializeState()
        assertTrue(stateJson.isNotBlank())

        val strategy2 = createStrategy(extractor)
        strategy2.restoreState(stateJson)

        val context = strategy2.buildContext(listOf(
            Message(id = "sys-1", role = Role.SYSTEM, content = "You are helpful"),
            msg1
        ))

        val systemMsg = context.first { it.role == Role.SYSTEM }
        assertTrue(systemMsg.content.contains("fitness app"))
        assertTrue(systemMsg.content.contains("Known facts"))
    }

    @Test
    fun factsUpdateCorrectly() = runTest {
        val extractor = FakeFactsExtractor()
        val strategy = createStrategy(extractor)

        extractor.nextFacts = Facts(goal = "app A")
        val msg1 = Message(id = "msg-1", role = Role.USER, content = "My goal is app A")
        strategy.onUserMessage(msg1)

        extractor.nextFacts = Facts(goal = "app B")
        val msg2 = Message(id = "msg-2", role = Role.USER, content = "My goal is app B")
        strategy.onUserMessage(msg2)

        val context = strategy.buildContext(listOf(
            Message(id = "sys-1", role = Role.SYSTEM, content = "You are helpful"),
            msg1, msg2
        ))

        val systemMsg = context.first { it.role == Role.SYSTEM }
        assertTrue(systemMsg.content.contains("app B"))
    }

    @Test
    fun factsIncludedInLlmContext() = runTest {
        val extractor = FakeFactsExtractor()
        val strategy = createStrategy(extractor)

        extractor.nextFacts = Facts(
            goal = "build a fitness app",
            constraints = "must work offline",
            preferences = "dark mode"
        )
        val msg1 = Message(id = "msg-1", role = Role.USER, content = "My goal is fitness app")
        strategy.onUserMessage(msg1)

        extractor.nextFacts = Facts(
            goal = "build a fitness app",
            constraints = "must work offline",
            preferences = "dark mode",
            decisions = "use Kotlin"
        )
        val msg2 = Message(id = "msg-2", role = Role.USER, content = "I want to use Kotlin")
        strategy.onUserMessage(msg2)

        val context = strategy.buildContext(listOf(
            Message(id = "sys-1", role = Role.SYSTEM, content = "You are helpful"),
            msg1,
            Message(id = "asst-1", role = Role.ASSISTANT, content = "Got it"),
            msg2
        ))

        val systemMsg = context.first { it.role == Role.SYSTEM }
        assertTrue(systemMsg.content.contains("Known facts"))
        assertTrue(systemMsg.content.contains("fitness app"))
        assertTrue(systemMsg.content.contains("offline"))
        assertTrue(systemMsg.content.contains("dark mode"))
        assertTrue(systemMsg.content.contains("Kotlin"))
    }

    @Test
    fun noDuplicateProcessing() = runTest {
        val extractor = FakeFactsExtractor()
        val strategy = createStrategy(extractor)

        extractor.nextFacts = Facts(goal = "fitness app")
        val msg1 = Message(id = "msg-1", role = Role.USER, content = "My goal is fitness app")

        strategy.onUserMessage(msg1)
        assertEquals(1, extractor.callCount)

        strategy.onUserMessage(msg1)
        assertEquals(1, extractor.callCount)
    }

    @Test
    fun restoreStateWithBlankStringDoesNotThrow() {
        val strategy = createStrategy()
        strategy.restoreState("")
        strategy.restoreState("   ")
        strategy.restoreState(null)
    }

    @Test
    fun serializeAndRestoreRoundTrip() = runTest {
        val extractor = FakeFactsExtractor()
        val strategy = createStrategy(extractor)

        extractor.nextFacts = Facts(
            goal = "build app",
            constraints = "mobile only",
            requirements = "fast"
        )
        val msg = Message(id = "msg-1", role = Role.USER, content = "My goal is build app")
        strategy.onUserMessage(msg)

        val serialized = strategy.serializeState()

        val strategy2 = createStrategy(extractor)
        strategy2.restoreState(serialized)

        val context = strategy2.buildContext(listOf(
            Message(id = "sys-1", role = Role.SYSTEM, content = "System"),
            msg
        ))

        val systemMsg = context.first { it.role == Role.SYSTEM }
        assertTrue(systemMsg.content.contains("build app"))
        assertTrue(systemMsg.content.contains("mobile only"))
        assertTrue(systemMsg.content.contains("fast"))
    }

    @Test
    fun noFactsShowsPlaceholder() {
        val strategy = createStrategy()
        val context = strategy.buildContext(listOf(
            Message(id = "sys-1", role = Role.SYSTEM, content = "You are helpful"),
            Message(id = "msg-1", role = Role.USER, content = "Hello")
        ))

        val systemMsg = context.first { it.role == Role.SYSTEM }
        assertTrue(systemMsg.content.contains("No facts recorded yet"))
    }

    @Test
    fun systemPromptPreservedWithFacts() = runTest {
        val extractor = FakeFactsExtractor()
        val strategy = createStrategy(extractor)

        extractor.nextFacts = Facts(goal = "test goal")
        val msg = Message(id = "msg-1", role = Role.USER, content = "My goal is test goal")
        strategy.onUserMessage(msg)

        val context = strategy.buildContext(listOf(
            Message(id = "sys-1", role = Role.SYSTEM, content = "You are a coding assistant"),
            msg
        ))

        val systemMsg = context.first { it.role == Role.SYSTEM }
        assertTrue(systemMsg.content.contains("You are a coding assistant"))
        assertTrue(systemMsg.content.contains("Known facts"))
        assertTrue(systemMsg.content.contains("test goal"))
    }

    @Test
    fun windowSizeLimitsNonSystemMessages() = runTest {
        val smallSettings = ContextSettings(
            strategy = ContextStrategyType.STICKY_FACTS,
            factsWindowSize = 2
        )
        val extractor = FakeFactsExtractor()
        val strategy = StickyFactsStrategy(smallSettings, extractor)

        extractor.nextFacts = Facts(goal = "test")
        strategy.onUserMessage(Message(id = "msg-1", role = Role.USER, content = "msg1"))

        val messages = listOf(
            Message(id = "sys-1", role = Role.SYSTEM, content = "System"),
            Message(id = "msg-1", role = Role.USER, content = "msg1"),
            Message(id = "asst-1", role = Role.ASSISTANT, content = "a1"),
            Message(id = "msg-2", role = Role.USER, content = "msg2"),
            Message(id = "asst-2", role = Role.ASSISTANT, content = "a2"),
            Message(id = "msg-3", role = Role.USER, content = "msg3")
        )

        val context = strategy.buildContext(messages)
        val nonSystem = context.filter { it.role != Role.SYSTEM }
        assertEquals(2, nonSystem.size)
    }

    @Test
    fun goalAndConstraintAccumulateInContext() = runTest {
        val extractor = FakeFactsExtractor()
        val strategy = createStrategy(extractor)

        extractor.nextFacts = Facts(goal = "build a fitness app")
        val msg1 = Message(id = "msg-1", role = Role.USER, content = "My goal is a fitness app")
        strategy.onUserMessage(msg1)

        extractor.nextFacts = Facts(goal = "build a fitness app", constraints = "must work offline")
        val msg2 = Message(id = "msg-2", role = Role.USER, content = "It must work offline")
        strategy.onUserMessage(msg2)

        val context = strategy.buildContext(listOf(
            Message(id = "sys-1", role = Role.SYSTEM, content = "You are helpful"),
            msg1,
            Message(id = "asst-1", role = Role.ASSISTANT, content = "OK"),
            msg2
        ))

        val systemMsg = context.first { it.role == Role.SYSTEM }
        assertTrue(systemMsg.content.contains("fitness app"), "Should contain goal")
        assertTrue(systemMsg.content.contains("offline"), "Should contain constraint")
        assertTrue(systemMsg.content.contains("Goal:"))
        assertTrue(systemMsg.content.contains("Constraints:"))
    }

    class FakeFactsExtractor : FactsExtractor {
        var callCount = 0
        var nextFacts: Facts = Facts()

        override suspend fun updateFacts(currentFacts: Facts, newMessage: String): Facts {
            callCount++
            return nextFacts
        }
    }
}
