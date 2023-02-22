package com.subreax.reaction.data.auth

import com.subreax.reaction.api.*
import com.subreax.reaction.utils.Return

class RemoteAuthDataSource(val api: BackendService) {
    suspend fun signIn(username: String, password: String): Return<AuthData> {
        return safeApiCall {
            api.signIn(SignInDto(username, password))
        }
    }

    suspend fun signUp(email: String, username: String, password: String): Return<Unit> {
        return safeApiCall {
            api.signUp(SignUpDto(email, username, password))
        }
    }

    suspend fun refreshToken(authData: AuthData): Return<AuthData> {
        return safeApiCall {
            api.refreshToken(authData.refreshToken)
        }
    }
}
