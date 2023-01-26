package com.subreax.reaction.data.chat

import com.subreax.reaction.api.User
import kotlinx.coroutines.flow.Flow

enum class MessageState { NoState, Pending, Sent }
data class Message(
    val chatId: String,
    val from: User,
    val content: String,
    val sentTime: Long,
    val state: MessageState = MessageState.NoState
)

data class Chat(
    val id: String,
    val avatar: String?,
    val title: String,
    val lastMessage: Message?,
    val isMuted: Boolean,
    val isPinned: Boolean,
    val membersCount: Int,
)


interface ChatRepository {
    suspend fun getChatsList(invalidateCache: Boolean): List<Chat>
    suspend fun getChatById(chatId: String): Chat?
    suspend fun getMessages(chatId: String): List<Message>
    suspend fun sendMessage(chatId: String, text: String)

    val onMessagesChanged: Flow<Chat>
}
