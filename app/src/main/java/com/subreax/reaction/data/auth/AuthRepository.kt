package com.subreax.reaction.data.auth

import android.content.Context
import com.subreax.reaction.api.ApiResult
import com.subreax.reaction.api.AuthData

interface AuthRepository {
    data class SignInData(val username: String, val password: String)
    data class SignUpData(val email: String, val username: String, val password: String)

    suspend fun init(ctx: Context) {  }

    suspend fun signIn(data: SignInData): ApiResult<Unit>
    suspend fun signUp(data: SignUpData): ApiResult<Unit>

    suspend fun getToken(): String
    fun getUserId(): String

    fun isSignedIn(): Boolean
}
