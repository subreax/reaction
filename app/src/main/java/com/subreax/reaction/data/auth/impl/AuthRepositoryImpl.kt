package com.subreax.reaction.data.auth.impl

import android.util.Log
import com.subreax.reaction.api.*
import com.subreax.reaction.data.auth.LocalAuthDataSource
import com.subreax.reaction.data.auth.AuthRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class AuthRepositoryImpl(
    private val api: BackendService,
    private val localAuthDataSource: LocalAuthDataSource
) : AuthRepository {
    private var authData: AuthData? = null
    private val tokenMutex = Mutex()

    private val _onAuthEvent = MutableStateFlow(false)
    override val onAuthEvent: Flow<Boolean>
        get() = _onAuthEvent.asStateFlow()

    init {
        authData = localAuthDataSource.load()
        if (authData != null) {
            with(CoroutineScope(Dispatchers.IO)) {
                launch {
                    _onAuthEvent.emit(true)
                }
            }
        }
    }

    override suspend fun signIn(data: AuthRepository.SignInData): ApiResult<Unit> {
        val result = safeApiCall(Dispatchers.IO) {
            api.signIn(SignInDto(data.username, data.password))
        }

        if (result is ApiResult.Success) {
            result.value.formatAccessToken().also {
                authData = it
                localAuthDataSource.save(it)
            }
        }

        return result.convert<Unit> {  }
    }

    override suspend fun signUp(data: AuthRepository.SignUpData): ApiResult<Unit> {
        return safeApiCall(Dispatchers.IO) {
            api.signUp(SignUpDto(data.email, data.username, data.password))
        }.convert<Unit> {  }
    }

    override suspend fun getToken(): String {
        tokenMutex.withLock {
            val ad = authData
            if (ad == null) {
                Log.e(TAG, "User is not signed in")
                return ""
            }

            if (!ad.isTokenAlive) {
                authData = refreshToken(ad).formatAccessToken().also {
                    localAuthDataSource.save(it)
                }
            }
        }

        return authData!!.accessToken
    }

    override fun getUserId(): String {
        return authData?.userId ?: ""
    }

    override fun isSignedIn(): Boolean {
        return authData != null
    }

    private suspend fun refreshToken(data: AuthData): AuthData {
        val newData = api.refreshToken(data.refreshToken)
        return newData.copy(userId = data.userId)
    }

    private fun AuthData.formatAccessToken() = copy(accessToken = "Bearer $accessToken")

    companion object {
        const val TAG = "AuthRepositoryImpl"
    }
}