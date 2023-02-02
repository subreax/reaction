package com.subreax.reaction

import android.app.Application
import android.util.Log
import com.subreax.reaction.data.AppContainer
import com.subreax.reaction.data.AppContainerImpl
import kotlinx.coroutines.runBlocking

class ReactionApplication : Application() {
    lateinit var appContainer: AppContainer

    override fun onCreate() {
        super.onCreate()
        Log.d("ReactionApplication", "application onCreate()")
        appContainer = AppContainerImpl()

        runBlocking {
            appContainer.authRepository.init(applicationContext)
        }
    }
}
