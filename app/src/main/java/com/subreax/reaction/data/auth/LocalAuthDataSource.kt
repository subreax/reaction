package com.subreax.reaction.data.auth

import android.content.SharedPreferences
import com.subreax.reaction.api.AuthData

class LocalAuthDataSource(private val prefs: SharedPreferences) {
    fun save(data: AuthData) {
        with(prefs.edit()) {
            putString(PREFS_ACCESS_TOKEN_KEY, data.accessToken)
            putLong(PREFS_ACCESS_TOKEN_EXP_KEY, data.accessTokenExp)
            putString(PREFS_REFRESH_TOKEN_KEY, data.refreshToken)
            putLong(PREFS_REFRESH_TOKEN_EXP_KEY, data.refreshTokenExp)

            if (data.userId != null) {
                putString(PREFS_USER_ID_KEY, data.userId)
            }

            apply()
        }
    }

    fun load(): AuthData? {
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

    private fun clearPrefs() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val PREFS_ACCESS_TOKEN_KEY = "access_token"
        private const val PREFS_ACCESS_TOKEN_EXP_KEY = "access_token_exp"
        private const val PREFS_REFRESH_TOKEN_KEY = "refresh_token"
        private const val PREFS_REFRESH_TOKEN_EXP_KEY = "refresh_token_exp"
        private const val PREFS_USER_ID_KEY = "user_id_key"
    }
}