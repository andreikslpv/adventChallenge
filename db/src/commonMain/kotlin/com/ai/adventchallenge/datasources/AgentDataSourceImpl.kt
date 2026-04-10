package com.ai.adventchallenge.datasources

import com.ai.adventchallenge.data.datasource.AgentDataSource
import com.ai.adventchallenge.db.dao.AgentDao
import com.ai.adventchallenge.db.entities.AgentEntity
import com.ai.adventchallenge.domain.model.Agent
import com.ai.adventchallenge.domain.model.AIModel
import com.ai.adventchallenge.domain.model.AgentSettings
import com.ai.adventchallenge.domain.model.AIProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AgentDataSourceImpl(
    private val agentDao: AgentDao
) : AgentDataSource {
    override fun getAllAgents(): Flow<List<Agent>> =
        agentDao.getAllAgents().map { entities ->
            entities.map { it.toDomainAgent() }
        }

    override suspend fun getAgentById(agentId: String): Agent? = 
        agentDao.getAgentById(agentId)?.toDomainAgent()

    override suspend fun insertAgent(agent: Agent) {
        val entity = agent.toEntity()
        agentDao.insertAgent(entity)
    }

    override suspend fun insertAgents(agents: List<Agent>) {
        val entities = agents.map { it.toEntity() }
        agentDao.insertAgents(entities)
    }

    override suspend fun updateAgent(agent: Agent) {
        val entity = agent.toEntity()
        agentDao.updateAgent(entity)
    }

    override suspend fun deleteAgent(agentId: String) = agentDao.deleteAgent(agentId)

    override suspend fun deleteAllAgents() = agentDao.deleteAllAgents()
}

private fun AgentEntity.toDomainAgent(): Agent {
    val provider = try {
        AIProvider.valueOf(providerName)
    } catch (_: Exception) {
        AIProvider.ZAI
    }
    val model = AIModel(
        provider = provider,
        modelName = modelName
    )
    return Agent(
        id = id,
        settings = AgentSettings(
            systemPrompt = systemPrompt,
            temperature = temperature,
            selectedModel = model,
            maxTokens = maxTokens,
            responseType = responseType,
            stopWord = stopWord
        )
    )
}

private fun Agent.toEntity(): AgentEntity {
    return AgentEntity(
        id = id,
        systemPrompt = settings.systemPrompt,
        temperature = settings.temperature,
        providerName = settings.selectedModel.provider.name,
        modelName = settings.selectedModel.modelName,
        maxTokens = settings.maxTokens,
        responseType = settings.responseType,
        stopWord = settings.stopWord
    )
}