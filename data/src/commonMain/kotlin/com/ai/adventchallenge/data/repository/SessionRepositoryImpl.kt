package com.ai.adventchallenge.data.repository

import com.ai.adventchallenge.data.datasource.SessionDataSource
import com.ai.adventchallenge.domain.context.ContextSettings
import com.ai.adventchallenge.domain.model.Session
import com.ai.adventchallenge.domain.repository.SessionRepository
import kotlinx.coroutines.flow.Flow

class SessionRepositoryImpl(
    private val sessionDataSource: SessionDataSource
) : SessionRepository {
    override fun getAllSessions(): Flow<List<Session>> =
        sessionDataSource.getAllSessions()

    override suspend fun getSessionById(id: String): Session? =
        sessionDataSource.getSessionById(id)

    override suspend fun saveSession(session: Session) {
        sessionDataSource.insertSession(session)
    }

    override suspend fun updateSessionSummary(id: String, summary: String) =
        sessionDataSource.updateSessionSummary(id, summary)

    override suspend fun updateSessionContextSettings(
        id: String,
        contextSettings: ContextSettings
    ) {
        sessionDataSource.updateSessionContextSettings(id, contextSettings)
    }

    override suspend fun updateStrategyState(id: String, strategyStateJson: String) =
        sessionDataSource.updateStrategyState(id, strategyStateJson)

    override suspend fun deleteSession(id: String) =
        sessionDataSource.deleteSession(id)

    override suspend fun deleteAllSessions() =
        sessionDataSource.deleteAllSessions()

    override suspend fun updateSessionName(id: String, name: String) =
        sessionDataSource.updateSessionName(id, name)
}
