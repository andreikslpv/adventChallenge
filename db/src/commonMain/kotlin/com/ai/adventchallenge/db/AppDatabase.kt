package com.ai.adventchallenge.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.ai.adventchallenge.db.dao.AgentDao
import com.ai.adventchallenge.db.dao.MessageDao
import com.ai.adventchallenge.db.dao.SessionDao
import com.ai.adventchallenge.db.entities.AgentEntity
import com.ai.adventchallenge.db.entities.MessageEntity
import com.ai.adventchallenge.db.entities.SessionEntity

const val DATABASE_NAME = "app_db"

@Database(
    entities = [AgentEntity::class, MessageEntity::class, SessionEntity::class],
    version = 5,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun agentDao(): AgentDao
    abstract fun messageDao(): MessageDao
    abstract fun sessionDao(): SessionDao
}