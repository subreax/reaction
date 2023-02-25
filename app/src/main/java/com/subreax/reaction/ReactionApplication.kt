package com.subreax.reaction

import android.app.Application
import android.app.NotificationManager
import android.content.Context
import android.util.Log
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import com.subreax.reaction.data.AppContainer
import com.subreax.reaction.data.AppContainerImpl

class ReactionApplication : Application(), ImageLoaderFactory {
    lateinit var appContainer: AppContainer

    override fun onCreate() {
        super.onCreate()
        appContainer = AppContainerImpl(applicationContext)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        FcmPushNotificationsService.registerChannel(applicationContext, notificationManager)
    }

    override fun newImageLoader(): ImageLoader {
        Log.d("ReactionApplication", "newImageLoader")
        return ImageLoader.Builder(applicationContext)
            .memoryCache(
                MemoryCache.Builder(applicationContext)
                    .maxSizePercent(0.25)
                    .build()
            )
            .diskCache(
                DiskCache.Builder()
                    .directory(cacheDir.resolve("img_cache"))
                    .maxSizePercent(0.1)
                    .build()
            )
            .respectCacheHeaders(false)
            .build()
    }
}
