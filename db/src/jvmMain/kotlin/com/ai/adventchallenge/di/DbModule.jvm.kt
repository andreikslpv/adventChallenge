package com.ai.adventchallenge.di

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.ai.adventchallenge.db.AppDatabase
import com.ai.adventchallenge.db.DATABASE_NAME
import com.ai.adventchallenge.db.dao.AgentDao
import com.ai.adventchallenge.db.dao.MessageDao
import com.ai.adventchallenge.db.dao.SessionDao
import kotlinx.coroutines.Dispatchers
import org.koin.dsl.module
import java.io.File

actual val dbPlatformModule = module {
    single<AppDatabase> {
        val dbFile = File(System.getProperty("user.home"), ".adventChallenge/$DATABASE_NAME")
        val dbDir = dbFile.parentFile
        if (!dbDir.exists()) {
            dbDir.mkdirs()
        }
        Room.databaseBuilder<AppDatabase>(
            name = dbFile.absolutePath
        )
            .fallbackToDestructiveMigration(true)
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(Dispatchers.IO)
            .build()
    }

    single<AgentDao> { get<AppDatabase>().agentDao() }
    single<MessageDao> { get<AppDatabase>().messageDao() }
    single<SessionDao> { get<AppDatabase>().sessionDao() }
}
