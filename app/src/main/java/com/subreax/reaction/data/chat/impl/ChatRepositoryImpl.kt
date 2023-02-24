package com.subreax.reaction.data.chat.impl

import android.util.Log
import com.subreax.reaction.api.*
import com.subreax.reaction.data.ApplicationStateSource
import com.subreax.reaction.data.SocketService
import com.subreax.reaction.data.auth.AuthRepository
import com.subreax.reaction.data.chat.*
import com.subreax.reaction.data.user.UserRepository
import com.subreax.reaction.utils.Return
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
    private val localChatDS: LocalChatDataSource,
    private val remoteChatDS: RemoteChatDataSource,
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val socketService: SocketService,
    private val appStateSrc: ApplicationStateSource
) : ChatRepository {
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    // chatId - list of members
    private val _chatMembers = mutableMapOf<String, List<User>>()

    // chatId - message
    private val _messages = mutableMapOf<String, MutableList<Message>>()

    private val _onMessagesChanged = MutableSharedFlow<Chat>()
    override val onMessagesChanged: Flow<Chat>
        get() = _onMessagesChanged

    override val onChatsChanged: Flow<Int>
        get() = localChatDS.onChatsChanged


    init {
        appStateSrc.addSyncingAction {
            syncChats()
            _messages.clear()
            true
        }

        observeOnMessage()
    }

    private suspend fun syncChats() {
        val chatsRet = remoteChatDS.getChatsList()
        if (chatsRet is Return.Ok) {
            localChatDS.setList(chatsRet.value)
        }
        else {
            val fail = chatsRet as Return.Fail
            Log.e(TAG, "Failed to sync chats: ${fail.code}  ${fail.message}")
        }
    }

    private fun observeOnMessage() {
        coroutineScope.launch {
            socketService.onMessage.collect { msg ->
                val chat = localChatDS.findById(msg.chatId)
                if (chat == null) {
                    Log.e(TAG, "Message from unknown chat: ${msg.chatId}")
                }
                else {
                    localChatDS.updateOne(chat.copy(lastMessage = msg))
                    _messages[chat.id]?.add(msg)
                    notifyMessagesChanged(chat)
                }
            }
        }
    }

    override suspend fun getChatsList(): List<Chat> {
        return localChatDS.getList()
            .sortedByDescending { it.lastMessage?.sentTime ?: System.currentTimeMillis() }
    }

    override suspend fun getChatById(chatId: String): Chat? {
        return localChatDS.findById(chatId)
    }

    override suspend fun getChatMembers(chatId: String): List<User> {
        if (!_chatMembers.containsKey(chatId)) {
            val membersRet = remoteChatDS.getChatMembers(chatId)
            if (membersRet is Return.Ok) {
                _chatMembers[chatId] = membersRet.value
            }
            else {
                val fail = membersRet as Return.Fail
                Log.e(TAG, "Failed to get members for the chat: ${fail.code}  ${fail.message}")
            }
        }
        return _chatMembers[chatId] ?: emptyList()
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
            val chatId = socketService.onCreateChat.waitForAny()
            val chatRet = remoteChatDS.getById(chatId)
            if (chatRet is Return.Ok) {
                localChatDS.updateOne(chatRet.value!!)
            }
            else {
                Log.d(TAG, "Failed to get details of the chat: $chatId")
            }
        }
    }

    override suspend fun joinChat(chatId: String) {
        withContext(Dispatchers.IO) {
            socketService.joinChat(chatId)
            socketService.onJoinChat.waitFor(chatId)
        }
    }

    override suspend fun leaveChat(chatId: String) {
        withContext(Dispatchers.IO) {
            socketService.leaveChat(chatId)
            socketService.onLeaveChat.waitFor(chatId)
            removeChat(chatId)
        }
    }

    override suspend fun isUserAMemberOfTheChat(chatId: String): Boolean {
        return localChatDS.findById(chatId) != null
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

        val chat = localChatDS.findById(chatId)?.copy(isMuted = !enabled) ?: return
        localChatDS.updateOne(chat)
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

    private fun notifyMessagesChanged(chat: Chat) {
        coroutineScope.launch {
            _onMessagesChanged.emit(chat)
        }
    }

    private suspend fun removeChat(chatId: String) {
        _chatMembers.remove(chatId)
        _messages.remove(chatId)
        localChatDS.remove(chatId)
    }

    companion object {
        private const val TAG = "ChatRepositoryImpl"
    }
}
