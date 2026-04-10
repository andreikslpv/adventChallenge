package com.ai.adventchallenge.data.repository

import com.ai.adventchallenge.data.datasource.AgentDataSource
import com.ai.adventchallenge.domain.model.Agent
import com.ai.adventchallenge.domain.repository.AgentRepository
import kotlinx.coroutines.flow.Flow

class AgentRepositoryImpl(
    private val agentDataSource: AgentDataSource
) : AgentRepository {
    override fun getAllAgents(): Flow<List<Agent>> = 
        agentDataSource.getAllAgents()

    override suspend fun getAgentById(agentId: String): Agent? = 
        agentDataSource.getAgentById(agentId)

    override suspend fun saveAgent(agent: Agent) {
        agentDataSource.insertAgent(agent)
    }

    override suspend fun saveAgents(agents: List<Agent>) {
        agentDataSource.insertAgents(agents)
    }

    override suspend fun deleteAgent(agentId: String) = 
        agentDataSource.deleteAgent(agentId)

    override suspend fun deleteAllAgents() = 
        agentDataSource.deleteAllAgents()
}
