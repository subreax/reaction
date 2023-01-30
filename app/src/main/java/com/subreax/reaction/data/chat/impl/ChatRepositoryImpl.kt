package com.subreax.reaction.data.chat.impl

import android.util.Log
import com.subreax.reaction.api.ApiResult
import com.subreax.reaction.api.BackendService
import com.subreax.reaction.api.safeApiCall
import com.subreax.reaction.api.unsafeApiCall
import com.subreax.reaction.data.SocketService
import com.subreax.reaction.data.auth.AuthRepository
import com.subreax.reaction.data.chat.Chat
import com.subreax.reaction.data.chat.ChatRepository
import com.subreax.reaction.data.chat.Message
import com.subreax.reaction.data.user.UserRepository
import com.subreax.reaction.putSynchronously
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

class ChatRepositoryImpl(
    private val api: BackendService,
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val socketService: SocketService
) : ChatRepository {
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    // chatId - chat
    private var _chats = mutableMapOf<String, Chat>()
    private var _isChatsLoaded = false

    // chatId - message
    private val _messages = mutableMapOf<String, MutableList<Message>>()

    private val _onMessagesChanged = MutableSharedFlow<Chat>()
    override val onMessagesChanged: Flow<Chat>
        get() = _onMessagesChanged

    private val _onChatsChanged = MutableSharedFlow<Int>()
    override val onChatsChanged: Flow<Int>
        get() = _onChatsChanged


    init {
        observeOnMessage()
        observeOnCreateChat()
    }

    private fun observeOnMessage() {
        coroutineScope.launch {
            socketService.onMessage.collect { msg ->
                val chat = _getChatMap()[msg.chatId]
                if (chat != null) {
                    _messages[msg.chatId]?.add(msg)
                    _getChatMap()[msg.chatId] = _getChatMap()[msg.chatId]!!.copy( // todo: !!
                        lastMessage = msg
                    )
                    _onMessagesChanged.emit(chat)
                } else {
                    Log.w("ChatRepositoryImpl", "Message from unknown chat: ${msg.chatId}")
                }
            }
        }
    }

    private fun observeOnCreateChat() {
        coroutineScope.launch {
            socketService.onCreateChat.collect { chatId ->
                _requestChats(_chats)
            }
        }
    }

    override suspend fun getChatsList(invalidateCache: Boolean): List<Chat> {
        return withContext(Dispatchers.IO) {
            if (invalidateCache) {
                _isChatsLoaded = false
            }
            _getChatMap()
                .map { it.value }
                .sortedByDescending { it.lastMessage?.sentTime ?: System.currentTimeMillis() }
        }
    }

    override suspend fun getChatById(chatId: String): Chat? {
        return _getChatMap()[chatId] ?: _requestChat(chatId)
    }

    override suspend fun getMessages(chatId: String): List<Message> {
        if (!_messages.containsKey(chatId)) {
            _messages[chatId] = _requestMessages(chatId).toMutableList()
        }
        return _messages[chatId]?.reversed() ?: emptyList()
    }

    override suspend fun sendMessage(chatId: String, text: String) {
        socketService.send(chatId, text)
    }

    override suspend fun createChat(name: String) {
        withContext(Dispatchers.IO) {
            socketService.createChat(name)
        }
    }

    override suspend fun joinChat(chatId: String) {
        withContext(Dispatchers.IO) {
            socketService.joinChat(chatId)
            delay(500) // todo
            launch {
                _requestChats(_chats)
            }
        }
    }

    override suspend fun leaveChat(chatId: String) {
        withContext(Dispatchers.IO) {
            socketService.leaveChat(chatId)
        }
    }

    override suspend fun isUserAMemberOfTheChat(chatId: String): Boolean {
        return _getChatMap()[chatId] != null
    }

    private suspend fun _requestChats(outChats: MutableMap<String, Chat>) {
        withContext(Dispatchers.IO) {
            outChats.clear()
            val chatsDto = unsafeApiCall {
                api.getChatList(authRepository.getToken())
            }

            coroutineScope {
                for (chatDto in chatsDto) {
                    launch {
                        val lastMessage = chatDto.lastMessage?.toMessage(userRepository)
                        val chatDetailsDto =
                            api.getChatDetails(authRepository.getToken(), chatDto.id)

                        _chats.putSynchronously(chatDto.id, Chat(
                            chatDto.id,
                            chatDto.avatar,
                            chatDto.title,
                            lastMessage,
                            chatDto.isMuted,
                            chatDto.isPinned,
                            chatDetailsDto.membersCount
                        ))
                    }
                }
            }

            _onChatsChanged.emit(0)
        }
    }

    // todo: finish it when api will change
    private suspend fun _requestChat(chatId: String): Chat? {
        return withContext(Dispatchers.IO) {
            val result = safeApiCall { api.getChatDetails(authRepository.getToken(), chatId) }
            return@withContext when (result) {
                is ApiResult.Success -> {
                    Chat(
                        chatId,
                        result.value.avatar,
                        result.value.title,
                        null,
                        result.value.isMuted,
                        result.value.isPinned,
                        result.value.membersCount
                    )
                }
                else -> {
                    null
                }
            }
        }
    }

    private suspend fun _getChatMap(): MutableMap<String, Chat> {
        if (!_isChatsLoaded) {
            _requestChats(_chats)
            _isChatsLoaded = true
        }
        return _chats
    }

    private suspend fun _requestMessages(chatId: String): List<Message> {
        return withContext(Dispatchers.IO) {
            val messagesDto = unsafeApiCall {
                api.getChatMessages(authRepository.getToken(), chatId).messages
            }

            messagesDto.map { it.toMessage(userRepository) }
        }
    }
}
