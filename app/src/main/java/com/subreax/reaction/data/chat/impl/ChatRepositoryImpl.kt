package com.subreax.reaction.data.chat.impl

import android.util.Log
import com.subreax.reaction.api.BackendService
import com.subreax.reaction.api.unsafeApiCall
import com.subreax.reaction.data.SocketService
import com.subreax.reaction.data.auth.AuthRepository
import com.subreax.reaction.data.chat.Chat
import com.subreax.reaction.data.chat.ChatRepository
import com.subreax.reaction.data.chat.Message
import com.subreax.reaction.data.user.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
    override val onMessagesChanged: Flow<Chat> = _onMessagesChanged.asSharedFlow()

    init {
        coroutineScope.launch {
            socketService.onMessage.collect { msg ->
                val chat = _chats[msg.chatId]
                if (chat == null) {
                    Log.w("ChatRepositoryImpl", "Message from unknown chat: ${msg.chatId}")
                } else {
                    _messages[msg.chatId]?.add(msg)
                    _chats[msg.chatId] = _chats[msg.chatId]!!.copy( // todo: !!
                        lastMessage = msg
                    )
                    /*
                    val messages = _messages[msg.chatId] ?: mutableListOf()
                    messages.add(msg)
                    _messages[msg.chatId] = messages*/
                    _onMessagesChanged.emit(chat)
                }
            }
        }
    }

    override suspend fun getChatsList(invalidateCache: Boolean): List<Chat> {
        return withContext(Dispatchers.IO) {
            if (invalidateCache) {
                _isChatsLoaded = false
                _chats.clear()
            }

            if (!_isChatsLoaded) {
                _requestChats(_chats)
                _isChatsLoaded = true
            }

            _chats.map { it.value }
        }
    }

    override suspend fun getChatById(chatId: String): Chat? {
        if (!_isChatsLoaded) {
            _requestChats(_chats)
            _isChatsLoaded = true
        }
        return _chats[chatId]
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

    private suspend fun _requestChats(outChats: MutableMap<String, Chat>) {
        withContext(Dispatchers.IO) {
            outChats.clear()
            val chatsDto = unsafeApiCall {
                api.getChatList(authRepository.getToken())
            }

            for (chatDto in chatsDto) {
                val lastMessage = chatDto.lastMessage?.toMessage(userRepository)
                val chatDetailsDto = api.getChatDetails(authRepository.getToken(), chatDto.id)

                outChats[chatDto.id] = Chat(
                    chatDto.id,
                    chatDto.avatar,
                    chatDto.title,
                    lastMessage,
                    chatDto.isMuted,
                    chatDto.isPinned,
                    chatDetailsDto.membersCount
                )
            }
        }
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
