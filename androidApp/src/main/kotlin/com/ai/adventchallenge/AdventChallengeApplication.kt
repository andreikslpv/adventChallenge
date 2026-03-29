package com.ai.adventchallenge

import android.app.Application
import com.ai.adventchallenge.di.initKoin

class AdventChallengeApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        initKoin()
    }
}
