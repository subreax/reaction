package com.subreax.reaction.data.auth.impl

import android.util.Log
import com.subreax.reaction.api.*
import com.subreax.reaction.data.auth.AuthRepository
import com.subreax.reaction.data.auth.AuthRepository.Companion.EMPTY_TOKEN
import com.subreax.reaction.data.auth.LocalAuthDataSource
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
        set(value) {
            field = value
            notifyTokenChanged(value)
        }

    private val tokenMutex = Mutex()
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    private val _onAuthEvent = MutableStateFlow(false)
    override val onAuthEvent: Flow<Boolean>
        get() = _onAuthEvent.asStateFlow()

    private val _onTokenChanged = MutableStateFlow(EMPTY_TOKEN)
    override val onTokenChanged: Flow<String>
        get() = _onTokenChanged.asStateFlow()

    init {
        authData = localAuthDataSource.load()
        if (authData != null) {
            onAuth()
        }
    }

    override suspend fun signIn(data: AuthRepository.SignInData): ApiResult<Unit> {
        val result = safeApiCall(Dispatchers.IO) {
            api.signIn(SignInDto(data.username, data.password))
        }

        if (result is ApiResult.Success) {
            setNewAuthData(result.value)
            onAuth()
        }

        return result.convert<Unit> { }
    }

    override suspend fun signUp(data: AuthRepository.SignUpData): ApiResult<Unit> {
        return safeApiCall(Dispatchers.IO) {
            api.signUp(SignUpDto(data.email, data.username, data.password))
        }.convert<Unit> { }
    }

    override suspend fun getToken(): String {
        tokenMutex.withLock {
            if (authData == null) {
                Log.e(TAG, "User is not signed in")
                return EMPTY_TOKEN
            }

            if (!authData!!.isTokenAlive) {
                refreshToken()
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

    private suspend fun refreshToken(): Boolean {
        return authData?.let { data ->
            val apiResult = safeApiCall { api.refreshToken(data.refreshToken) }
            if (apiResult is ApiResult.Success) {
                setNewAuthData(apiResult.value)
                Log.d(TAG, "Token has refreshed")
                true
            } else {
                Log.e(TAG, "Failed to refresh token: ${apiResult.errorToString()}")
                false
            }
        } ?: false
    }

    private fun setNewAuthData(newData: AuthData) {
        val userId = newData.userId ?: getUserId()
        val newData1 = newData.copy(userId = userId, accessToken = formatAccessToken(newData.accessToken))
        localAuthDataSource.save(newData1)
        authData = newData1
    }

    private fun onAuth() {
        coroutineScope.launch {
            _onAuthEvent.emit(true)
        }
    }

    private fun formatAccessToken(at: String) = "Bearer $at"

    private fun notifyTokenChanged(data: AuthData?) {
        coroutineScope.launch {
            _onTokenChanged.emit(data?.accessToken ?: EMPTY_TOKEN)
        }
    }

    companion object {
        private const val TAG = "AuthRepositoryImpl"
    }
}
