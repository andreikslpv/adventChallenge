package com.ai.adventchallenge.data.datasources

import com.ai.adventchallenge.db.entities.AgentEntity
import kotlinx.coroutines.flow.Flow

interface AgentDataSource {
    fun getAllAgents(): Flow<List<AgentEntity>>
    suspend fun getAgentById(agentId: String): AgentEntity?
    suspend fun insertAgent(agent: AgentEntity)
    suspend fun insertAgents(agents: List<AgentEntity>)
    suspend fun updateAgent(agent: AgentEntity)
    suspend fun deleteAgent(agentId: String)
    suspend fun deleteAllAgents()
}
