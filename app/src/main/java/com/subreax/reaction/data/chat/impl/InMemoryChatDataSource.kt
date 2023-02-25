package com.subreax.reaction.data.chat.impl

import com.subreax.reaction.data.chat.Chat
import com.subreax.reaction.data.chat.LocalChatDataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class InMemoryChatDataSource : LocalChatDataSource {
    private val _onChatChanged = MutableSharedFlow<String>()
    override val onChatChanged: Flow<String>
        get() = _onChatChanged

    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private val chatMap = mutableMapOf<String, Chat>()
    private val mutex = Mutex()

    override suspend fun load() {
    }

    override fun getList(): List<Chat> {
        return chatMap.map { it.value }
    }

    override suspend fun setList(chats: List<Chat>) {
        mutex.withLock {
            chatMap.clear()
            for (chat in chats) {
                chatMap[chat.id] = chat
            }
        }
        notifyChatsChanged()
    }

    override suspend fun updateOne(chat: Chat) {
        mutex.withLock {
            if (chatMap.containsKey(chat.id)) {
                chatMap[chat.id] = chat
                notifyChatChanged(chat.id)
            }
            else {
                chatMap[chat.id] = chat
                notifyChatsChanged()
            }
        }
    }

    override suspend fun remove(chatId: String) {
        mutex.withLock {
            if (chatMap.containsKey(chatId)) {
                chatMap.remove(chatId)
                notifyChatsChanged()
            }
        }
    }

    override suspend fun findById(chatId: String): Chat? {
        return chatMap[chatId]
    }

    private fun notifyChatsChanged() {
        coroutineScope.launch {
            _onChatChanged.emit("")
        }
    }

    private fun notifyChatChanged(chatId: String) {
        coroutineScope.launch {
            _onChatChanged.emit(chatId)
        }
    }
}