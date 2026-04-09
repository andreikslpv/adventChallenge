package com.ai.adventchallenge.datasources

import com.ai.adventchallenge.db.dao.AgentDao
import com.ai.adventchallenge.db.entities.AgentEntity
import kotlinx.coroutines.flow.Flow

class AgentDataSourceImpl(
    private val agentDao: AgentDao
) : AgentDataSource {
    override fun getAllAgents(): Flow<List<AgentEntity>> = agentDao.getAllAgents()

    override suspend fun getAgentById(agentId: String): AgentEntity? = agentDao.getAgentById(agentId)

    override suspend fun insertAgent(agent: AgentEntity) = agentDao.insertAgent(agent)

    override suspend fun insertAgents(agents: List<AgentEntity>) = agentDao.insertAgents(agents)

    override suspend fun updateAgent(agent: AgentEntity) = agentDao.updateAgent(agent)

    override suspend fun deleteAgent(agentId: String) = agentDao.deleteAgent(agentId)

    override suspend fun deleteAllAgents() = agentDao.deleteAllAgents()
}
