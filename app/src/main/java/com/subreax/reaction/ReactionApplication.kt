package com.subreax.reaction

import android.app.Application
import android.app.NotificationManager
import android.content.Context
import android.util.Log
import com.subreax.reaction.data.AppContainer
import com.subreax.reaction.data.AppContainerImpl

class ReactionApplication : Application() {
    lateinit var appContainer: AppContainer

    override fun onCreate() {
        super.onCreate()
        Log.d("ReactionApplication", "application onCreate()")

        appContainer = AppContainerImpl(applicationContext)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        FcmPushNotificationsService.registerChannel(applicationContext, notificationManager)
    }
}
