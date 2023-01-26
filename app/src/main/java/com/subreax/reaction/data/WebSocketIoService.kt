package com.subreax.reaction.data

import android.util.Log
import com.google.gson.Gson
import com.subreax.reaction.api.MessageDto
import com.subreax.reaction.data.auth.AuthRepository
import com.subreax.reaction.data.chat.Message
import com.subreax.reaction.data.user.UserRepository
import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import org.json.JSONObject

class WebSocketIoService(
    private val url: String,
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : SocketService {

    private val _headers = mutableMapOf<String, List<String>>()

    private val _coroutineScope = CoroutineScope(Dispatchers.IO)
    private var _socket: Socket? = null
    private var _connectionId = ""


    private val _onMessage = MutableSharedFlow<Message>()
    override val onMessage: Flow<Message> = _onMessage.asSharedFlow()


    override fun start() {
        if (_socket != null) {
            return
        }

        _coroutineScope.launch(Dispatchers.IO) {
            _headers["Authorization"] = listOf(authRepository.getToken())

            val options = IO.Options.builder()
                .setExtraHeaders(_headers)
                .build()

            _socket = IO.socket(url, options).apply {
                on("onConnection", this@WebSocketIoService::eventOnConnection)
                on("onException", this@WebSocketIoService::eventOnException)
                on("onSendMessage", this@WebSocketIoService::eventOnMessage)
                connect()
            }
        }
    }

    override fun send(chatId: String, msgText: String) {
        val json = JSONObject()
        json.put("text", msgText)
        json.put("userId", authRepository.getUserId())
        json.put("roomId", chatId)
        json.put("date", System.currentTimeMillis())
        json.put("type", "text")
        _socket?.emit("newMessage", json)
    }

    override fun stop() {
        _socket?.disconnect()
        _socket = null
    }

    private fun eventOnConnection(args: Array<Any>) {
        if (args.isNotEmpty()) {
            val obj = args[0] as JSONObject
            _connectionId = obj["connectionId"] as String
            Log.d(TAG, "onConnection: $_connectionId")

            val req = JSONObject()
            req.put("userId", authRepository.getUserId())
            _socket?.emit("connectToRooms", req)
        }
    }

    private fun eventOnMessage(args: Array<Any>) {
        _coroutineScope.launch {
            if (args.isNotEmpty()) {
                val obj = args[0] as JSONObject
                val json = obj.getJSONObject("message").toString()
                val messageDto = Gson().fromJson(json, MessageDto::class.java)
                val message = messageDto.toMessage(userRepository)
                Log.d(TAG, "onMessage: ${message.content}")
                _onMessage.emit(message)
            }
        }
    }

    private fun eventOnException(args: Array<Any>) {
        Log.e(TAG, "onException")
        if (args.isNotEmpty()) {
            Log.e(TAG, (args[0] as JSONObject).toString())
        }
    }


    companion object {
        private const val TAG = "WebSocketService"
    }
}
