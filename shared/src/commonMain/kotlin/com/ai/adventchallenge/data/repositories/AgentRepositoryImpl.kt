package com.ai.adventchallenge.data.repositories

import com.ai.adventchallenge.agent.Agent
import com.ai.adventchallenge.agent.AgentSettings
import com.ai.adventchallenge.data.api.AIModel
import com.ai.adventchallenge.data.api.AIProvider
import com.ai.adventchallenge.data.datasources.AgentDataSource
import com.ai.adventchallenge.domain.repositories.AgentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.collections.map

class AgentRepositoryImpl(
    private val agentDataSource: AgentDataSource
) : AgentRepository {
    override fun getAllAgents(): Flow<List<Agent>> = 
        agentDataSource.getAllAgents().map { entities ->
            entities.map { entity ->
                entity.toDomainAgent()
            }
        }

    override suspend fun getAgentById(agentId: String): Agent? = 
        agentDataSource.getAgentById(agentId)?.toDomainAgent()

    override suspend fun saveAgent(agent: Agent) {
        val entity = AgentEntity.fromDomain(
            id = agent.id,
            systemPrompt = agent.settings.systemPrompt,
            temperature = agent.settings.temperature,
            providerName = agent.settings.selectedModel.provider.name,
            modelName = agent.settings.selectedModel.modelName,
            maxTokens = agent.settings.maxTokens,
            responseType = agent.settings.responseType,
            stopWord = agent.settings.stopWord
        )
        agentDataSource.insertAgent(entity)
    }

    override suspend fun saveAgents(agents: List<Agent>) {
        val entities = agents.map { agent ->
            AgentEntity.fromDomain(
                id = agent.id,
                systemPrompt = agent.settings.systemPrompt,
                temperature = agent.settings.temperature,
                providerName = agent.settings.selectedModel.provider.name,
                modelName = agent.settings.selectedModel.modelName,
                maxTokens = agent.settings.maxTokens,
                responseType = agent.settings.responseType,
                stopWord = agent.settings.stopWord
            )
        }
        agentDataSource.insertAgents(entities)
    }

    override suspend fun deleteAgent(agentId: String) = 
        agentDataSource.deleteAgent(agentId)

    override suspend fun deleteAllAgents() = 
        agentDataSource.deleteAllAgents()
}

private fun AgentEntity.toDomainAgent(): Agent {
    val provider = try {
        AIProvider.valueOf(providerName)
    } catch (e: Exception) {
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
