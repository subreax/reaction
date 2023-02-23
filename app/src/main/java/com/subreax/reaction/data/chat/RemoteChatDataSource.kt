package com.subreax.reaction.data.chat

import com.subreax.reaction.api.User
import com.subreax.reaction.utils.Return

interface RemoteChatDataSource {
    suspend fun getById(chatId: String): Return<Chat?>
    suspend fun getChatsList(): Return<List<Chat>>
    suspend fun getChatMembers(chatId: String): Return<List<User>>
}