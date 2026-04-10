package com.ai.adventchallenge.di

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.ai.adventchallenge.db.AppDatabase
import com.ai.adventchallenge.db.DATABASE_NAME
import com.ai.adventchallenge.db.dao.AgentDao
import com.ai.adventchallenge.db.dao.MessageDao
import kotlinx.coroutines.Dispatchers
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module

actual val dbPlatformModule = module {
    single<AppDatabase> {
        val dbFile = androidApplication().getDatabasePath(DATABASE_NAME)
        Room.databaseBuilder<AppDatabase>(
            context = androidApplication(),
            name = dbFile.absolutePath
        )
            .fallbackToDestructiveMigration(true)
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(Dispatchers.IO)
            .build()
    }

    single<AgentDao> { get<AppDatabase>().agentDao() }
    single<MessageDao> { get<AppDatabase>().messageDao() }
}
