package com.subreax.reaction.data.auth.impl

import android.util.Log
import com.subreax.reaction.api.*
import com.subreax.reaction.data.auth.AuthRepository
import com.subreax.reaction.data.auth.AuthRepository.Companion.EMPTY_TOKEN
import com.subreax.reaction.data.auth.LocalAuthDataSource
import com.subreax.reaction.data.auth.RemoteAuthDataSource
import com.subreax.reaction.utils.Return
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class AuthRepositoryImpl(
    private val remoteAuthDataSource: RemoteAuthDataSource,
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

    override suspend fun signIn(username: String, password: String): Return<Unit> {
        val ret = remoteAuthDataSource.signIn(username, password)

        return when (ret) {
            is Return.Ok -> {
                setNewAuthData(ret.value)
                onAuth()
                Return.Ok(Unit)
            }
            is Return.Fail -> {
                ret
            }
        }
    }

    override suspend fun signUp(email: String, username: String, password: String): Return<Unit> {
        return remoteAuthDataSource.signUp(email, username, password)
    }

    override suspend fun getToken(): String {
        var token = EMPTY_TOKEN
        withContext(Dispatchers.IO) {
            tokenMutex.withLock {
                if (authData == null) {
                    Log.e(TAG, "User is not signed in")
                    return@withContext
                }
                
                if (!authData!!.isTokenAlive) {
                    refreshToken()
                }
                token = authData!!.accessToken
            }
        }
        return token
    }

    override fun getUserId(): String {
        return authData?.userId ?: ""
    }

    override fun isSignedIn(): Boolean {
        return authData != null
    }

    private suspend fun refreshToken(): Boolean {
        return authData?.let { data ->
            val ret = remoteAuthDataSource.refreshToken(data)

            when (ret) {
                is Return.Ok -> {
                    setNewAuthData(ret.value)
                    Log.d(TAG, "Token has refreshed")
                    true
                }
                is Return.Fail -> {
                    Log.e(TAG, "Failed to refresh token: ${ret.message}")
                    false
                }
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
