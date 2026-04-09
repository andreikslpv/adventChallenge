package com.ai.adventchallenge.di

import org.koin.core.module.Module
import org.koin.dsl.module

val dbModule: Module = module {
    includes(dbPlatformModule)
}

expect val dbPlatformModule: Module
