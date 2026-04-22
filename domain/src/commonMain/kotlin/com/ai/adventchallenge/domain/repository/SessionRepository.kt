package com.ai.adventchallenge.domain.repository

import com.ai.adventchallenge.domain.context.ContextSettings
import com.ai.adventchallenge.domain.model.Session
import kotlinx.coroutines.flow.Flow

interface SessionRepository {
    fun getAllSessions(): Flow<List<Session>>
    suspend fun getSessionById(id: String): Session?
    suspend fun saveSession(session: Session)
    suspend fun updateSessionSummary(id: String, summary: String)
    suspend fun updateSessionContextSettings(id: String, contextSettings: ContextSettings)
    suspend fun deleteSession(id: String)
    suspend fun deleteAllSessions()
    suspend fun updateSessionName(id: String, name: String)
}