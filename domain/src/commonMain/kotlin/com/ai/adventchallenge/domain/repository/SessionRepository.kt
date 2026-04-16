package com.ai.adventchallenge.domain.repository

import com.ai.adventchallenge.domain.model.Session
import kotlinx.coroutines.flow.Flow

interface SessionRepository {
    fun getAllSessions(): Flow<List<Session>>
    suspend fun getSessionById(id: String): Session?
    suspend fun saveSession(session: Session)
    suspend fun deleteSession(id: String)
    suspend fun deleteAllSessions()
    suspend fun updateSessionName(id: String, name: String)
}