package com.ai.adventchallenge.di

import com.ai.adventchallenge.domain.repository.AgentRepository
import com.ai.adventchallenge.domain.repository.MessageRepository
import com.ai.adventchallenge.data.repository.AgentRepositoryStub
import com.ai.adventchallenge.data.repository.MessageRepositoryStub
import org.koin.dsl.module

actual val platformModule = module {
    single<AgentRepository> { AgentRepositoryStub() }
    single<MessageRepository> { MessageRepositoryStub() }
}
