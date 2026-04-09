package com.ai.adventchallenge.domain.repositories

import com.ai.adventchallenge.agent.Agent
import kotlinx.coroutines.flow.Flow

interface AgentRepository {
    fun getAllAgents(): Flow<List<Agent>>
    suspend fun getAgentById(agentId: String): Agent?
    suspend fun saveAgent(agent: Agent)
    suspend fun saveAgents(agents: List<Agent>)
    suspend fun deleteAgent(agentId: String)
    suspend fun deleteAllAgents()
}
