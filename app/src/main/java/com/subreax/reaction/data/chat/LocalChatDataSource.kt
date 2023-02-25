package com.subreax.reaction.data.chat

import kotlinx.coroutines.flow.Flow

interface LocalChatDataSource {
    // emits id for the chat that is changed.
    // if id is empty, update all the chats
    val onChatChanged: Flow<String>

    suspend fun load()
    fun getList(): List<Chat>
    suspend fun setList(chats: List<Chat>)
    suspend fun updateOne(chat: Chat)
    suspend fun remove(chatId: String)
    suspend fun findById(chatId: String): Chat?
}
