package com.subreax.reaction.data.auth

import com.subreax.reaction.utils.Return
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun signIn(username: String, password: String): Return<Unit>
    suspend fun signUp(email: String, username: String, password: String): Return<Unit>

    suspend fun getToken(): String
    fun getUserId(): String

    fun isSignedIn(): Boolean

    val onAuthEvent: Flow<Boolean>
    val onTokenChanged: Flow<String>

    companion object {
        const val EMPTY_TOKEN = ""
    }
}
