package com.ai.adventchallenge.data.datasource

import com.ai.adventchallenge.domain.model.Agent
import kotlinx.coroutines.flow.Flow

interface AgentDataSource {
    fun getAllAgents(): Flow<List<Agent>>
    suspend fun getAgentById(agentId: String): Agent?
    suspend fun insertAgent(agent: Agent)
    suspend fun insertAgents(agents: List<Agent>)
    suspend fun updateAgent(agent: Agent)
    suspend fun deleteAgent(agentId: String)
    suspend fun deleteAllAgents()
}
