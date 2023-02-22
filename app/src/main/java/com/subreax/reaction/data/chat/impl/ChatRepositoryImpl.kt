package com.subreax.reaction.data.chat.impl

import android.util.Log
import com.subreax.reaction.api.*
import com.subreax.reaction.data.ApplicationStateSource
import com.subreax.reaction.data.SocketService
import com.subreax.reaction.data.auth.AuthRepository
import com.subreax.reaction.data.chat.Chat
import com.subreax.reaction.data.chat.ChatRepository
import com.subreax.reaction.data.chat.Message
import com.subreax.reaction.data.user.UserRepository
import com.subreax.reaction.utils.Return
import com.subreax.reaction.utils.putSynchronously
import com.subreax.reaction.utils.waitFor
import com.subreax.reaction.utils.waitForAny
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChatRepositoryImpl(
    private val api: BackendService,
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val socketService: SocketService,
    private val appStateSrc: ApplicationStateSource
) : ChatRepository {
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    // chatId - chat
    private val chatCache = mutableMapOf<String, Chat>()

    // chatId - list of members
    private val _chatMembers = mutableMapOf<String, List<User>>()

    private var _isChatsCached = false

    // chatId - message
    private val _messages = mutableMapOf<String, MutableList<Message>>()

    private val _onMessagesChanged = MutableSharedFlow<Chat>()
    override val onMessagesChanged: Flow<Chat>
        get() = _onMessagesChanged

    private val _onChatsChanged = MutableSharedFlow<Int>()
    override val onChatsChanged: Flow<Int>
        get() = _onChatsChanged


    init {
        appStateSrc.addSyncingAction {
            _getChatMap(invalidateCache = true)
            _messages.clear()
            true
        }

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
                _getChatMap(true)
            }
        }
    }

    override suspend fun getChatsList(invalidateCache: Boolean): List<Chat> {
        return withContext(Dispatchers.IO) {
            _getChatMap()
                .map { it.value }
                .sortedByDescending { it.lastMessage?.sentTime ?: System.currentTimeMillis() }
        }
    }

    override suspend fun getChatById(chatId: String): Chat? {
        return _getChatMap()[chatId] ?: _requestChat(chatId)
    }

    override suspend fun getChatMembers(chatId: String): List<User> {
        if (!_chatMembers.containsKey(chatId)) {
            val members = unsafeApiCall { api.getChatMembers(authRepository.getToken(), chatId) }
            val userList = members.map {
                userRepository.getUserById(it.userId)!!
            }
            _chatMembers[chatId] = userList
        }

        return _chatMembers[chatId]!!
    }

    override suspend fun getMessages(chatId: String): List<Message> {
        if (!_messages.containsKey(chatId)) {
            _messages[chatId] = fetchMessages(chatId).toMutableList()
        }
        return _messages[chatId]?.reversed() ?: emptyList()
    }

    override suspend fun sendMessage(chatId: String, text: String) {
        socketService.send(chatId, text)
    }

    override suspend fun createChat(name: String) {
        withContext(Dispatchers.IO) {
            socketService.createChat(name)
            socketService.onCreateChat.waitForAny()
        }
    }

    override suspend fun joinChat(chatId: String) {
        withContext(Dispatchers.IO) {
            socketService.joinChat(chatId)
            socketService.onJoinChat.waitFor(chatId)
            launch {
                fetchChats(chatCache)
            }
        }
    }

    override suspend fun leaveChat(chatId: String) {
        withContext(Dispatchers.IO) {
            socketService.leaveChat(chatId)
            socketService.onLeaveChat.waitFor(chatId)
            _deleteChatLocally(chatId)
        }
    }

    override suspend fun isUserAMemberOfTheChat(chatId: String): Boolean {
        return _getChatMap()[chatId] != null
    }

    override suspend fun toggleNotifications(chatId: String, enabled: Boolean) {
        val token = authRepository.getToken()
        val chatPtr = ChatPointer1Dto(chatId, authRepository.getUserId())
        if (enabled) {
            unsafeApiCall { api.unmuteChat(token, chatPtr) }
        }
        else {
            unsafeApiCall { api.muteChat(token, chatPtr) }
        }

        chatCache[chatId] = chatCache[chatId]?.copy(
            isMuted = !enabled
        ) ?: return
        _notifyChatsChanged()
    }

    private suspend fun fetchChats(outChats: MutableMap<String, Chat>) {
        withContext(Dispatchers.IO) {
            outChats.clear()
            val ret = safeApiCall {
                api.getChatList(authRepository.getToken())
            }

            if (ret is Return.Ok) {
                for (chatDto in ret.value) {
                    val lastMessage = chatDto.lastMessage?.toMessage(userRepository)

                    chatCache.putSynchronously(chatDto.id, Chat(
                        chatDto.id,
                        chatDto.avatar,
                        chatDto.title,
                        chatDto.membersCount,
                        lastMessage,
                        chatDto.isMuted,
                        chatDto.isPinned,
                    ))
                }
                _notifyChatsChanged()
            }
        }
    }

    private suspend fun _requestChat(chatId: String): Chat? {
        return withContext(Dispatchers.IO) {
            val ret = safeApiCall { api.getChatDetails(authRepository.getToken(), chatId) }
            return@withContext when (ret) {
                is Return.Ok -> {
                    Log.d("ChatRepositoryImpl", "Invite to: $ret")
                    Chat(
                        chatId,
                        ret.value.avatar,
                        ret.value.title,
                        ret.value.membersCount,
                        null,
                        ret.value.isMuted,
                        ret.value.isPinned,
                    )
                }
                else -> {
                    null
                }
            }
        }
    }

    private suspend fun _getChatMap(invalidateCache: Boolean = false): MutableMap<String, Chat> {
        if (invalidateCache) {
            _isChatsCached = false
        }

        if (!_isChatsCached) {
            fetchChats(chatCache)
            _isChatsCached = true
        }
        return chatCache
    }

    private suspend fun fetchMessages(chatId: String): List<Message> {
        return withContext(Dispatchers.IO) {
            val ret = safeApiCall {
                api.getChatMessages(authRepository.getToken(), chatId).messages
            }

            if (ret is Return.Ok) {
                ret.value.map { it.toMessage(userRepository) }
            }
            else {
                emptyList()
            }
        }
    }

    private fun _notifyChatsChanged() {
        coroutineScope.launch {
            _onChatsChanged.emit(0)
        }
    }

    private fun _deleteChatLocally(chatId: String) {
        chatCache.remove(chatId)
        _chatMembers.remove(chatId)
        _messages.remove(chatId)
        _notifyChatsChanged()
    }
}
