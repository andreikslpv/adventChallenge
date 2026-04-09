package com.ai.adventchallenge.data.repositories

import com.ai.adventchallenge.agent.Agent
import com.ai.adventchallenge.domain.repositories.AgentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class AgentRepositoryStub : AgentRepository {
    private val agents = MutableStateFlow<List<Agent>>(emptyList())

    override fun getAllAgents(): Flow<List<Agent>> = agents

    override suspend fun getAgentById(agentId: String): Agent? = 
        agents.value.find { it.id == agentId }

    override suspend fun saveAgent(agent: Agent) {
        val current = agents.value.toMutableList()
        val index = current.indexOfFirst { it.id == agent.id }
        if (index >= 0) {
            current[index] = agent
        } else {
            current.add(agent)
        }
        agents.value = current
    }

    override suspend fun saveAgents(agents: List<Agent>) {
        this.agents.value = agents
    }

    override suspend fun deleteAgent(agentId: String) {
        agents.value = agents.value.filterNot { it.id == agentId }
    }

    override suspend fun deleteAllAgents() {
        agents.value = emptyList()
    }
}
