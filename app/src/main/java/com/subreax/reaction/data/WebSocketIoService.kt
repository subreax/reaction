package com.subreax.reaction.data

import android.util.Log
import com.google.gson.Gson
import com.subreax.reaction.api.MessageDto
import com.subreax.reaction.data.auth.AuthRepository
import com.subreax.reaction.data.chat.Message
import com.subreax.reaction.data.user.UserRepository
import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
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
    override val onMessage: Flow<Message>
        get() = _onMessage.asSharedFlow()

    private val _onCreateChat = MutableSharedFlow<String>()
    override val onCreateChat: Flow<String>
        get() = _onCreateChat.asSharedFlow()

    private val _onJoinChat = MutableSharedFlow<String>()
    override val onJoinChat: Flow<String>
        get() = _onJoinChat.asSharedFlow()

    private val _onLeaveChat = MutableSharedFlow<String>()
    override val onLeaveChat: Flow<String>
        get() = _onLeaveChat.asSharedFlow()

    init {
        listenTokenChanges()
    }

    override fun start() {
        if (_socket != null) {
            Log.w(TAG, "start(): Socket is already opened")
            return
        }

        _coroutineScope.launch(Dispatchers.IO) {
            val options = IO.Options.builder()
                .setExtraHeaders(_headers)
                .build()

            _socket = IO.socket(url, options).apply {
                on("onConnection", this@WebSocketIoService::eventOnConnection)
                on("onException", this@WebSocketIoService::eventOnException)
                on("onSendMessage", this@WebSocketIoService::eventOnMessage)
                on("onCreateRoom", this@WebSocketIoService::eventOnCreateChat)
                on("onJoinToRoom", this@WebSocketIoService::eventOnJoinChat)
                on("onLeaveFromRoom", this@WebSocketIoService::eventOnLeaveChat)
                connect()
            }

            Log.d(TAG, "Socket has opened")
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

        Log.d(TAG, "emit newMessage")
    }

    override fun createChat(name: String) {
        val json = JSONObject()
        json.put("userId", authRepository.getUserId())
        json.put("name", name)
        _socket?.emit("createRoom", json)

        Log.d(TAG, "emit createRoom")
    }

    override fun joinChat(chatId: String) {
        val json = JSONObject()
        json.put("userId", authRepository.getUserId())
        json.put("roomId", chatId)
        _socket?.emit("joinToRoom", json)

        Log.d(TAG, "emit joinToRoom")
    }

    override fun leaveChat(chatId: String) {
        val json = JSONObject()
        json.put("userId", authRepository.getUserId())
        json.put("roomId", chatId)
        _socket?.emit("leaveFromRoom", json)

        Log.d(TAG, "emit leaveFromRoom")
    }

    override fun stop() {
        _socket?.let {
            it.disconnect()
            Log.d(TAG, "Socket has closed")
            _socket = null
        }
    }

    private fun connectToRooms() {
        val json = JSONObject()
        json.put("userId", authRepository.getUserId())
        _socket?.emit("connectToRooms", json)

        Log.d(TAG, "emit connectToRooms")
    }

    private fun eventOnConnection(args: Array<Any>) {
        Log.d(TAG, "event onConnection")
        if (args.isNotEmpty()) {
            val obj = args[0] as JSONObject
            _connectionId = obj["connectionId"] as String

            connectToRooms()
        }
    }

    private fun eventOnMessage(args: Array<Any>) {
        _coroutineScope.launch {
            Log.d(TAG, "event onMessage")
            if (args.isNotEmpty()) {
                val obj = args[0] as JSONObject
                val json = obj.getJSONObject("message").toString()
                val messageDto = Gson().fromJson(json, MessageDto::class.java)
                val message = messageDto.toMessage(userRepository)
                _onMessage.emit(message)
            }
        }
    }

    private fun eventOnCreateChat(args: Array<Any>) {
        _coroutineScope.launch {
            Log.d(TAG, "event onCreateChat")
            if (args.isNotEmpty()) {
                val json = args[0] as JSONObject
                val chatId = json.getString("roomId")
                _onCreateChat.emit(chatId)
            }
        }
    }

    private fun eventOnJoinChat(args: Array<Any>) {
        Log.d(TAG, "event onJoinChat")
        if (args.isNotEmpty()) {
            val json = args[0] as JSONObject
            val chatId = json.getString("roomId")
            _coroutineScope.launch {
                _onJoinChat.emit(chatId)
            }
        }
    }

    private fun eventOnLeaveChat(args: Array<Any>) {
        Log.d(TAG, "event onLeaveChat")
        if (args.isNotEmpty()) {
            val json = args[0] as JSONObject
            val chatId = json.getString("roomId")
            _coroutineScope.launch {
                _onLeaveChat.emit(chatId)
            }
        }
    }

    private fun eventOnException(args: Array<Any>) {
        Log.e(TAG, "event onException")
        if (args.isNotEmpty()) {
            Log.e(TAG, (args[0] as JSONObject).toString())
        }
    }

    private fun listenTokenChanges() {
        _coroutineScope.launch {
            authRepository.onTokenChanged.collect { token ->
                stop()

                if (token != AuthRepository.EMPTY_TOKEN) {
                    _headers["Authorization"] = listOf(token)
                    start()
                }
            }
        }
    }


    companion object {
        private const val TAG = "WebSocketIoService"
    }
}
