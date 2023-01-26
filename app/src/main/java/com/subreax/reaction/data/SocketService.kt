package com.subreax.reaction.data

import com.subreax.reaction.data.chat.Message
import kotlinx.coroutines.flow.Flow

interface SocketService {
    val onMessage: Flow<Message>

    fun start()
    fun send(chatId: String, msgText: String)
    fun stop()
}
