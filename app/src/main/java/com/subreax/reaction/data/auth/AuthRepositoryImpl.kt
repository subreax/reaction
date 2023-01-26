package com.subreax.reaction.data.auth

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.subreax.reaction.api.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AuthRepositoryImpl(
    private val api: BackendService,
    private val onSignedIn: (AuthRepository) -> Unit
) : AuthRepository {
    private var authData: AuthData? = null
    private lateinit var prefs: SharedPreferences

    override suspend fun init(ctx: Context) {
        prefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        withContext(Dispatchers.IO) {
            authData = loadAuthData()
            if (authData != null) {
                onSignedIn(this@AuthRepositoryImpl)
            }
        }
    }

    override suspend fun signIn(data: AuthRepository.SignInData): ApiResult<Unit> {
        val result = safeApiCall(Dispatchers.IO) {
            api.signIn(SignInDto(data.username, data.password))
        }

        if (result is ApiResult.Success) {
            authData = result.value
            saveAuthData(result.value)
        }

        return result.convert<Unit> {  }
    }

    override suspend fun signUp(data: AuthRepository.SignUpData): ApiResult<Unit> {
        return safeApiCall(Dispatchers.IO) {
            api.signUp(SignUpDto(data.email, data.username, data.password))
        }.convert<Unit> {  }
    }

    override suspend fun getToken(): String {
        if (authData == null) {
            Log.e(TAG, "User is not signed in")
            return ""
        }

        if (!authData!!.isTokenAlive) {
            authData = refreshToken(authData!!).also {
                saveAuthData(it)
            }
        }

        return "Bearer ${authData!!.accessToken}"
    }

    private suspend fun refreshToken(data: AuthData): AuthData {
        val newData = api.refreshToken(data.refreshToken)
        return newData.copy(userId = data.userId)
    }

    override fun getUserId(): String {
        return authData?.userId ?: ""
    }

    override fun isSignedIn(): Boolean {
        return authData != null
    }

    private fun loadAuthData(): AuthData? {
        val accessToken = prefs.getString(PREFS_ACCESS_TOKEN_KEY, null)
        val accessTokenExp = prefs.getLong(PREFS_ACCESS_TOKEN_EXP_KEY, 0L)
        val refreshToken = prefs.getString(PREFS_REFRESH_TOKEN_KEY, null)
        val refreshTokenExp = prefs.getLong(PREFS_REFRESH_TOKEN_EXP_KEY, 0)
        val userId = prefs.getString(PREFS_USER_ID_KEY, null)

        if (
            accessToken == null || accessTokenExp == 0L ||
            refreshToken == null || refreshTokenExp == 0L ||
            userId == null
        ) {
            clearPrefs()
            return null
        }

        return AuthData(
            userId = userId,
            accessToken = accessToken,
            accessTokenExp = accessTokenExp,
            refreshToken = refreshToken,
            refreshTokenExp = refreshTokenExp
        )
    }

    private fun saveAuthData(data: AuthData) {
        val editor = prefs.edit()
            .putString(PREFS_ACCESS_TOKEN_KEY, data.accessToken)
            .putLong(PREFS_ACCESS_TOKEN_EXP_KEY, data.accessTokenExp)
            .putString(PREFS_REFRESH_TOKEN_KEY, data.refreshToken)
            .putLong(PREFS_REFRESH_TOKEN_EXP_KEY, data.refreshTokenExp)

        if (data.userId != null) {
            editor.putString(PREFS_USER_ID_KEY, data.userId)
        }

        editor.apply()
    }

    private fun clearPrefs() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val TAG = "AuthRepositoryImpl"
        private const val PREFS_NAME = "auth_data"
        private const val PREFS_ACCESS_TOKEN_KEY = "access_token"
        private const val PREFS_ACCESS_TOKEN_EXP_KEY = "access_token_exp"
        private const val PREFS_REFRESH_TOKEN_KEY = "refresh_token"
        private const val PREFS_REFRESH_TOKEN_EXP_KEY = "refresh_token_exp"
        private const val PREFS_USER_ID_KEY = "user_id_key"
    }
}