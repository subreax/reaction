package com.subreax.reaction.data

import com.subreax.reaction.data.chat.Message
import kotlinx.coroutines.flow.Flow

interface SocketService {
    data class ChatNameChange(val chatId: String, val newName: String)

    val onMessage: Flow<Message>
    val onCreateChat: Flow<String>
    val onJoinChat: Flow<String>
    val onLeaveChat: Flow<String>
    val onChatNameChanged: Flow<ChatNameChange>

    suspend fun start()
    fun send(chatId: String, msgText: String)
    fun createChat(name: String)
    fun joinChat(chatId: String)
    fun leaveChat(chatId: String)
    fun setChatName(chatId: String, newName: String)
    fun stop()
}
