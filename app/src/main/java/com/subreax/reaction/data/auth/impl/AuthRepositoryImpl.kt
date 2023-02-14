package com.subreax.reaction.data.auth.impl

import android.content.Context
import android.util.Log
import androidx.work.*
import com.subreax.reaction.AppContainerHolder
import com.subreax.reaction.api.*
import com.subreax.reaction.data.auth.LocalAuthDataSource
import com.subreax.reaction.data.auth.AuthRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.TimeUnit
import kotlin.math.max

class AuthRepositoryImpl(
    private val api: BackendService,
    private val localAuthDataSource: LocalAuthDataSource,
    private val workManager: WorkManager
) : AuthRepository {
    private var authData: AuthData? = null
        set(value) {
            notifyTokenChanged(value)
            field = value
        }

    private val tokenMutex = Mutex()
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    private val _onAuthEvent = MutableStateFlow(false)
    override val onAuthEvent: Flow<Boolean>
        get() = _onAuthEvent.asStateFlow()

    private val _onTokenChanged = MutableStateFlow("")
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
            scheduleRefreshTokenWork()
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

    suspend fun refreshToken(): Boolean {
        return authData?.let { data ->
            val apiResult = safeApiCall { api.refreshToken(data.refreshToken) }
            if (apiResult is ApiResult.Success) {
                setNewAuthData(apiResult.value)
                Log.d(TAG, "Token has refreshed")
                true
            }
            else {
                Log.e(TAG, "Failed to refresh token: ${apiResult.errorToString()}")
                false
            }
        } ?: false
    }

    private fun setNewAuthData(newData: AuthData) {
        val userId = newData.userId ?: getUserId()
        val newData1 = newData.copy(userId = userId, accessToken = formatAccessToken(newData.accessToken))
        authData = newData1
        localAuthDataSource.save(newData1)
    }

    private fun onAuth() {
        coroutineScope.launch {
            _onAuthEvent.emit(true)
        }
    }

    private fun scheduleRefreshTokenWork() {
        authData?.let {
            Log.d(TAG, "Scheduling refresh token work")
            val initialDelay = max(
                it.remainingLifetime - 60 * 1000,
                PeriodicWorkRequest.MIN_PERIODIC_INTERVAL_MILLIS
            )

            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            // token is alive for 30 minutes
            val work = PeriodicWorkRequestBuilder<RefreshTokenWorker>(28, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
                .build()

            workManager.enqueueUniquePeriodicWork(RefreshTokenWorker.NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                work
            )
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
        const val EMPTY_TOKEN = ""
    }
}


class RefreshTokenWorker(appContext: Context, workerParameters: WorkerParameters) :
    CoroutineWorker(appContext, workerParameters) {
    override suspend fun doWork(): Result {
        val appContainer = AppContainerHolder.getInstance(applicationContext)
        val authRepository = appContainer.authRepository as AuthRepositoryImpl
        val isOk = withContext(Dispatchers.IO) {
            authRepository.refreshToken()
        }

        if (isOk) {
            return Result.success()
        }
        return Result.failure()
    }

    companion object {
        const val NAME = "refresh_token_work"
        private const val TAG = "RefreshTokenWorker"
    }
}