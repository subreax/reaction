package com.subreax.reaction.data.auth.impl

import com.subreax.reaction.api.ApiResult
import com.subreax.reaction.data.auth.AuthRepository
import com.subreax.reaction.exceptions.SignInException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

class FakeAuthRepository : AuthRepository {
    private var _isSignedIn = false

    override suspend fun signIn(data: AuthRepository.SignInData): ApiResult<Unit> {
        delay(1000)
        if (data.username != "refrigerator2k" || data.password != "wasd") {
            throw SignInException()
        }
        _isSignedIn = true
        return ApiResult.Success(Unit)
    }

    override suspend fun signUp(data: AuthRepository.SignUpData): ApiResult<Unit> {
        delay(1000)
        return ApiResult.Success(Unit)
    }

    override suspend fun getToken(): String {
        return "token"
    }

    override fun getUserId(): String {
        return "userId"
    }

    override fun isSignedIn(): Boolean = _isSignedIn
}
