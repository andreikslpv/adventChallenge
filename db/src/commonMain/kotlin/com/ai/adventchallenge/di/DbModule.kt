package com.ai.adventchallenge.di

import com.ai.adventchallenge.data.datasource.AgentDataSource
import com.ai.adventchallenge.data.datasource.MessageDataSource
import com.ai.adventchallenge.datasources.AgentDataSourceImpl
import com.ai.adventchallenge.datasources.MessageDataSourceImpl
import org.koin.core.module.Module
import org.koin.dsl.module

val dbModule: Module = module {
    includes(dbPlatformModule)

    single<AgentDataSource> { AgentDataSourceImpl(get()) }
    single<MessageDataSource> { MessageDataSourceImpl(get()) }
}

expect val dbPlatformModule: Module
