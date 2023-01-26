package com.subreax.reaction.data

import android.util.Log
import com.subreax.reaction.data.auth.AuthRepository
import com.subreax.reaction.data.chat.Message
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import okhttp3.*
import org.json.JSONObject


class OkHttpWsService(
    private val url: String,
    private val client: OkHttpClient,
    private val authRepository: AuthRepository
) : SocketService, WebSocketListener() {

    private lateinit var ws: WebSocket
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    override val onMessage: Flow<Message>
        get() = TODO("Not yet implemented")

    override fun start() {
        coroutineScope.launch {
            val request = Request.Builder()
                .url(url)
                .header("Authorization", authRepository.getToken())
                .build()

            ws = client.newWebSocket(request, this@OkHttpWsService)
        }
    }


    override fun onOpen(webSocket: WebSocket, response: Response) {
        Log.d(TAG, "onOpen")

        val data = JSONObject()
        data.put("userId", authRepository.getUserId())

        val text = JSONObject()
        text.put("event", "connectToRooms")
        text.put("data", data)

        Log.d(TAG, "Trying to send ${text.toString()}")
        webSocket.send(text.toString())
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        Log.d(TAG, "onMessage: $text")
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        Log.e(TAG, "onFailure", t)
    }


    override fun send(msgText: String, chatId: String) {

    }

    override fun stop() {

    }

    companion object {
        private const val TAG = "OkHttpWsService"
    }
}