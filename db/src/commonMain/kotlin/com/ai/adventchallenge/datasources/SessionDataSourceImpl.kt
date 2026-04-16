package com.ai.adventchallenge.datasources

import com.ai.adventchallenge.data.datasource.SessionDataSource
import com.ai.adventchallenge.db.dao.SessionDao
import com.ai.adventchallenge.db.entities.SessionEntity
import com.ai.adventchallenge.domain.model.Session
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

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

    override suspend fun deleteSession(id: String) = sessionDao.deleteSession(id)

    override suspend fun deleteAllSessions() = sessionDao.deleteAllSessions()

    override suspend fun updateSessionName(id: String, name: String) = 
        sessionDao.updateSessionName(id, name)
}

private fun SessionEntity.toDomainSession(): Session {
    return Session(
        id = id,
        name = name,
        createdAt = createdAt,
        selectedAgentId = selectedAgentId
    )
}

private fun Session.toEntity(): SessionEntity {
    return SessionEntity(
        id = id,
        name = name,
        createdAt = createdAt,
        selectedAgentId = selectedAgentId
    )
}