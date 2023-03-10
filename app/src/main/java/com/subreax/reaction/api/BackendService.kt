package com.subreax.reaction.api

import com.google.gson.annotations.SerializedName
import com.subreax.reaction.data.chat.Chat
import com.subreax.reaction.data.chat.Message
import com.subreax.reaction.data.chat.MessageState
import com.subreax.reaction.data.user.UserRepository
import okhttp3.*
import retrofit2.http.*
import java.lang.Long.max

interface BackendService {
    @POST("auth/sign-in")
    suspend fun signIn(@Body signInDto: SignInDto): AuthData

    @POST("auth/sign-up")
    suspend fun signUp(@Body signUpDto: SignUpDto)

    @GET("auth/update-refresh-token")
    suspend fun refreshToken(@Query("token") refreshToken: String): AuthData


    @GET("user/getDetails/{userId}")
    suspend fun getUserDetails(
        @Header("Authorization") token: String,
        @Path("userId") userId: String
    ): User


    @GET("room/getUserRooms")
    suspend fun getChatList(
        @Header("Authorization") token: String
    ): List<ChatDto>

    @GET("room/roomDetails/{chatId}")
    suspend fun getChatDetails(
        @Header("Authorization") token: String,
        @Path("chatId") chatId: String
    ): ChatDto

    @GET("room/members/{chatId}")
    suspend fun getChatMembers(
        @Header("Authorization") token: String,
        @Path("chatId") chatId: String
    ): List<MemberDto>

    @GET("room/roomChat/{chatId}")
    suspend fun getChatMessages(
        @Header("Authorization") token: String,
        @Path("chatId") chatId: String
    ): ChatMessagesDto


    @PUT("room/muteRoom")
    suspend fun muteChat(
        @Header("Authorization") token: String,
        @Body chatPtr: ChatPointer1Dto
    )

    @PUT("room/unmuteRoom")
    suspend fun unmuteChat(
        @Header("Authorization") token: String,
        @Body chatPtr: ChatPointer1Dto
    )
}


data class SignInDto(
    val username: String,
    val password: String,
    val rememberMe: Boolean = true,
    val authStrategy: String = "jwt"
)

data class SignUpDto(
    val email: String,
    val username: String,
    val password: String
)

data class AuthData(
    @SerializedName("userId")
    val userId: String?,

    @SerializedName("access_token")
    val accessToken: String,

    @SerializedName("expires")
    val accessTokenExp: Long,

    @SerializedName("refresh_token")
    val refreshToken: String,

    @SerializedName("refresh_token_expires")
    val refreshTokenExp: Long
) {
    val remainingLifetime: Long
        get() = max(accessTokenExp - System.currentTimeMillis(), 0L)

    val isTokenAlive: Boolean
        get() = remainingLifetime > 0L
}


data class ChatDto(
    @SerializedName("roomId", alternate = ["id"])
    val id: String,

    @SerializedName("avatarUrl")
    val avatar: String?,

    @SerializedName("name", alternate = ["title"])
    val title: String,

    @SerializedName("lastMessage")
    val lastMessageDto: MessageDto?,

    @SerializedName("membersCount")
    val membersCount: Int,

    @SerializedName("isMuted")
    val isMuted: Boolean,

    @SerializedName("isPinned")
    val isPinned: Boolean,
) {
    suspend fun toChat(userRepository: UserRepository): Chat {
        return Chat(
            id,
            avatar,
            title,
            membersCount,
            lastMessageDto?.toMessage(userRepository),
            isMuted,
            isPinned,
        )
    }
}

data class MemberDto(
    @SerializedName("userId")
    val userId: String,

    @SerializedName("role")
    val role: Int
)

data class MessageDto(
    @SerializedName("userId")
    val userId: String,

    @SerializedName("roomId")
    val chatId: String?,

    @SerializedName("text")
    val content: String?,

    @SerializedName("date")
    val sentTime: Long
) {
    suspend fun toMessage(userRepository: UserRepository): Message {
        return Message(
            chatId = chatId ?: "",
            from = userRepository.getUserById(userId)!!,
            content = content ?: "",
            sentTime = sentTime,
            state = MessageState.NoState
        )
    }
}

data class ChatMessagesDto(
    @SerializedName("messages")
    val messages: List<MessageDto>
)

data class User(
    @SerializedName("id")
    val id: String,

    @SerializedName("username")
    val name: String,

    @SerializedName("avatarUrl")
    val avatar: String?,

    @SerializedName("lastActivity")
    val lastActivity: Long
)

data class ChatPointer1Dto(
    @SerializedName("roomId")
    val chatId: String,

    @SerializedName("userId")
    val userId: String
)
