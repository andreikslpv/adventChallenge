package com.ai.adventchallenge.db

import androidx.room.Database
import androidx.room.RoomDatabase

const val DATABASE_NAME = "app_db"

@Database(
    entities = [],
    version = 1,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase()