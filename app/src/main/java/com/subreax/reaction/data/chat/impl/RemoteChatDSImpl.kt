package com.subreax.reaction.data.chat.impl

import com.subreax.reaction.api.BackendService
import com.subreax.reaction.api.MemberDto
import com.subreax.reaction.api.User
import com.subreax.reaction.api.safeApiCall
import com.subreax.reaction.data.auth.AuthRepository
import com.subreax.reaction.data.chat.Chat
import com.subreax.reaction.data.chat.RemoteChatDataSource
import com.subreax.reaction.data.user.UserRepository
import com.subreax.reaction.utils.Return

class RemoteChatDSImpl(
    private val api: BackendService,
    private val auth: AuthRepository,
    private val userRepository: UserRepository
) : RemoteChatDataSource {
    override suspend fun getById(chatId: String): Return<Chat?> {
        val ret = safeApiCall { api.getChatDetails(auth.getToken(), chatId) }
        return when (ret) {
            is Return.Ok -> {
                Return.Ok(ret.value.toChat(userRepository))
            }
            is Return.Fail -> {
                ret
            }
        }
    }

    override suspend fun getChatsList(): Return<List<Chat>> {
        val ret = safeApiCall { api.getChatList(auth.getToken()) }
        return when (ret) {
            is Return.Ok -> {
                val list = ret.value.map { it.toChat(userRepository) }
                Return.Ok(list)
            }
            is Return.Fail -> {
                ret
            }
        }
    }

    override suspend fun getChatMembers(chatId: String): Return<List<User>> {
        val ret = safeApiCall { api.getChatMembers(auth.getToken(), chatId) }
        return when (ret) {
            is Return.Ok -> {
                val dtoList = ret.value
                val result = dtoList.map { it.toUser() }
                return Return.Ok(result)
            }
            is Return.Fail -> {
                ret
            }
        }
    }

    private suspend fun MemberDto.toUser(): User {
        return userRepository.getUserById(userId) ?: unknownUser(userId)
    }

    private fun unknownUser(userId: String): User {
        return User(userId, "хтоя#$userId", null, 0)
    }
}