package com.subreax.reaction

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.*
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.Person
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.subreax.reaction.api.AuthData
import com.subreax.reaction.api.User
import com.subreax.reaction.data.AppContainer
import com.subreax.reaction.data.chat.Chat
import com.subreax.reaction.data.chat.Message
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.cancellable


class FcmPushNotificationsService : FirebaseMessagingService() {
    private lateinit var notificationManager: NotificationManager

    override fun onCreate() {
        super.onCreate()

        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    override fun onNewToken(token: String) {
        Log.d("FcmPushNotificationsSvc", "Refreshed token: $token")
    }

    override fun onMessageReceived(message: RemoteMessage) {
        /*val chat = message.data["chat"] ?: "unknown chat"
        val author = message.data["author"] ?: "no author"
        val text = message.data["text"] ?: "no text"
        showNotification()*/
    }

    private fun showNotification(chat: String, author: String, text: String) {
        /*val notification =
            NotificationCompat.Builder(applicationContext, MESSAGES_NOTIFICATION_CHANNEL_ID)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setContentTitle(chat)
                .setContentText("$temp. $type")
                .setSmallIcon(R.drawable.baseline_message_24)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build()

        notificationManager.notify(1, notification)*/
    }

    companion object {
        const val MESSAGES_NOTIFICATION_CHANNEL_ID = "MessagesNotificationChannel"

        fun registerChannel(
            context: Context,
            notificationManager: NotificationManager
        ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    MESSAGES_NOTIFICATION_CHANNEL_ID,
                    context.getString(R.string.messages_notification_channel_name),
                    NotificationManager.IMPORTANCE_HIGH
                )
                notificationManager.createNotificationChannel(channel)
            }
        }
    }
}
