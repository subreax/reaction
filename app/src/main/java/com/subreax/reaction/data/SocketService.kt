package com.subreax.reaction.data

import com.subreax.reaction.data.chat.Message
import kotlinx.coroutines.flow.Flow

interface SocketService {
    val onMessage: Flow<Message>
    val onCreateChat: Flow<String>

    fun start()
    fun send(chatId: String, msgText: String)
    fun createChat(name: String)
    fun joinChat(chatId: String)
    fun leaveChat(chatId: String)
    fun stop()
}
