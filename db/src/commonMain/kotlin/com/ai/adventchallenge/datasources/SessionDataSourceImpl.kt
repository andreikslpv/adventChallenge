package com.ai.adventchallenge.datasources

import com.ai.adventchallenge.data.datasource.SessionDataSource
import com.ai.adventchallenge.db.dao.SessionDao
import com.ai.adventchallenge.db.entities.SessionEntity
import com.ai.adventchallenge.domain.context.ContextSettings
import com.ai.adventchallenge.domain.context.ContextStrategyType
import com.ai.adventchallenge.domain.model.Session
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json

class SessionDataSourceImpl(
    private val sessionDao: SessionDao
) : SessionDataSource {
    override fun getAllSessions(): Flow<List<Session>> =
        sessionDao.getAllSessions().map { entities ->
            entities.map { it.toDomainSession() }
        }

    override suspend fun getSessionById(id: String): Session? =
        sessionDao.getSessionById(id)?.toDomainSession()

    override suspend fun insertSession(session: Session) {
        val entity = session.toEntity()
        sessionDao.insertSession(entity)
    }

    override suspend fun updateSessionSummary(id: String, summary: String) =
        sessionDao.updateSessionSummary(id, summary)

    override suspend fun updateSessionContextSettings(
        id: String,
        contextSettings: ContextSettings
    ) {
        val contextSettingsJson = try {
            json.encodeToString(contextSettings)
        } catch (_: Exception) {
            ""
        }
        sessionDao.updateSessionContextSettings(id, contextSettingsJson)
    }

    override suspend fun updateStrategyState(id: String, strategyStateJson: String) =
        sessionDao.updateStrategyState(id, strategyStateJson)

    override suspend fun updateCurrentMessageId(id: String, currentMessageId: String) =
        sessionDao.updateCurrentMessageId(id, currentMessageId)

    override suspend fun deleteSession(id: String) = sessionDao.deleteSession(id)

    override suspend fun deleteAllSessions() = sessionDao.deleteAllSessions()

    override suspend fun updateSessionName(id: String, name: String) =
        sessionDao.updateSessionName(id, name)
}

private val json = Json {
    ignoreUnknownKeys = true
    isLenient = true
}

private fun SessionEntity.toDomainSession(): Session {
    val contextSettings = if (contextSettingsJson.isNotEmpty()) {
        try {
            json.decodeFromString<ContextSettings>(contextSettingsJson)
        } catch (_: Exception) {
            ContextSettings(strategy = ContextStrategyType.FULL_HISTORY)
        }
    } else {
        ContextSettings(strategy = ContextStrategyType.FULL_HISTORY)
    }

    return Session(
        id = id,
        name = name,
        createdAt = createdAt,
        selectedAgentId = selectedAgentId,
        summary = summary,
        contextSettings = contextSettings,
        strategyStateJson = strategyStateJson,
        currentMessageId = currentMessageId
    )
}

private fun Session.toEntity(): SessionEntity {
    val contextSettingsJson = try {
        json.encodeToString(contextSettings)
    } catch (_: Exception) {
        ""
    }

    return SessionEntity(
        id = id,
        name = name,
        createdAt = createdAt,
        selectedAgentId = selectedAgentId,
        summary = summary,
        contextSettingsJson = contextSettingsJson,
        strategyStateJson = strategyStateJson,
        currentMessageId = currentMessageId
    )
}
