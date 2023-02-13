package com.subreax.reaction

import android.app.Application
import android.content.Context
import android.util.Log
import com.subreax.reaction.data.AppContainer
import com.subreax.reaction.data.AppContainerImpl

object AppContainerHolder {
    private var instance: AppContainer? = null

    fun getInstance(appContext: Context): AppContainer {
        synchronized(this) {
            if (instance == null) {
                instance = AppContainerImpl(appContext)
            }
            return instance!!
        }
    }
}


class ReactionApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Log.d("ReactionApplication", "application onCreate()")
        AppContainerHolder.getInstance(applicationContext)
    }
}
