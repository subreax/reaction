package com.subreax.reaction.data.auth

import com.subreax.reaction.api.ApiResult
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    data class SignInData(val username: String, val password: String)
    data class SignUpData(val email: String, val username: String, val password: String)

    suspend fun signIn(data: SignInData): ApiResult<Unit>
    suspend fun signUp(data: SignUpData): ApiResult<Unit>

    suspend fun getToken(): String
    fun getUserId(): String

    fun isSignedIn(): Boolean

    val onAuthEvent: Flow<Boolean>
}
