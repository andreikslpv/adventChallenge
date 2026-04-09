package com.ai.adventchallenge.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.ai.adventchallenge.db.entities.AgentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AgentDao {
    @Query("SELECT * FROM agents")
    fun getAllAgents(): Flow<List<AgentEntity>>

    @Query("SELECT * FROM agents WHERE id = :agentId")
    suspend fun getAgentById(agentId: String): AgentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAgent(agent: AgentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAgents(agents: List<AgentEntity>)

    @Update
    suspend fun updateAgent(agent: AgentEntity)

    @Query("DELETE FROM agents WHERE id = :agentId")
    suspend fun deleteAgent(agentId: String)

    @Query("DELETE FROM agents")
    suspend fun deleteAllAgents()
}
