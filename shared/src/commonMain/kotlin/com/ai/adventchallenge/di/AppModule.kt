package com.ai.adventchallenge.di

import com.ai.adventchallenge.data.di.dataModule
import com.ai.adventchallenge.data.repository.AgentRepositoryImpl
import com.ai.adventchallenge.data.repository.MessageRepositoryImpl
import com.ai.adventchallenge.data.repository.SessionRepositoryImpl
import com.ai.adventchallenge.data.service.AIServiceImpl
import com.ai.adventchallenge.domain.repository.AgentRepository
import com.ai.adventchallenge.domain.repository.MessageRepository
import com.ai.adventchallenge.domain.repository.SessionRepository
import com.ai.adventchallenge.domain.service.AIService
import com.ai.adventchallenge.domain.service.MainAgent
import com.ai.adventchallenge.domain.service.SummarizerAgent

import com.ai.adventchallenge.platform.ApiKeyProvider
import com.ai.adventchallenge.viewmodel.ChatViewModel
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

fun initKoin(appDeclaration: KoinAppDeclaration? = null) {
    startKoin {
        modules(listOf(appModule, dataModule, platformModule))
        appDeclaration?.let { it() }
    }
}

val appModule = module {

    single { ApiKeyProvider() }
    single<AIService> { AIServiceImpl(get()) }
    single<AgentRepository> { AgentRepositoryImpl(get()) }
    single<MessageRepository> { MessageRepositoryImpl(get()) }
    single<SessionRepository> { SessionRepositoryImpl(get()) }
    single { MainAgent(get()) }
    single { SummarizerAgent(get()) }
    single { ChatViewModel(get(), get(), get(), get(), get()) }
}

expect val platformModule: Module
