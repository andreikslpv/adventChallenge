package com.ai.adventchallenge.di

import com.ai.adventchallenge.domain.repositories.AgentRepository
import com.ai.adventchallenge.domain.repositories.MessageRepository
import com.ai.adventchallenge.data.repositories.AgentRepositoryStub
import com.ai.adventchallenge.data.repositories.MessageRepositoryStub
import org.koin.dsl.module

actual val platformModule = module {
    single<AgentRepository> { AgentRepositoryStub() }
    single<MessageRepository> { MessageRepositoryStub() }
}
