package com.ai.adventchallenge.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ai.adventchallenge.db.entities.SessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {
    @Query("SELECT * FROM sessions ORDER BY createdAt DESC")
    fun getAllSessions(): Flow<List<SessionEntity>>

    @Query("SELECT * FROM sessions WHERE id = :id")
    suspend fun getSessionById(id: String): SessionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: SessionEntity)

    @Query("UPDATE sessions SET summary = :summary WHERE id = :id")
    suspend fun updateSessionSummary(id: String, summary: String)

    @Query("UPDATE sessions SET contextSettingsJson = :contextSettingsJson WHERE id = :id")
    suspend fun updateSessionContextSettings(id: String, contextSettingsJson: String)

    @Query("UPDATE sessions SET strategyStateJson = :strategyStateJson WHERE id = :id")
    suspend fun updateStrategyState(id: String, strategyStateJson: String)

    @Query("DELETE FROM sessions WHERE id = :id")
    suspend fun deleteSession(id: String)

    @Query("DELETE FROM sessions")
    suspend fun deleteAllSessions()

    @Query("UPDATE sessions SET name = :name WHERE id = :id")
    suspend fun updateSessionName(id: String, name: String)
}
