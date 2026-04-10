package com.ai.adventchallenge

import android.app.Application
import com.ai.adventchallenge.di.initKoin
import org.koin.android.ext.koin.androidContext

class App : Application() {

    override fun onCreate() {
        super.onCreate()

        initKoin {
            this.androidContext(this@App)
        }
    }
}
