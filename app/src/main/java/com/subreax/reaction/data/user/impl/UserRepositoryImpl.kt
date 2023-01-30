package com.subreax.reaction.data.user.impl

import com.subreax.reaction.api.ApiResult
import com.subreax.reaction.api.BackendService
import com.subreax.reaction.api.User
import com.subreax.reaction.api.safeApiCall
import com.subreax.reaction.data.auth.AuthRepository
import com.subreax.reaction.data.user.UserRepository
import com.subreax.reaction.putSynchronously
import kotlinx.coroutines.Dispatchers

class UserRepositoryImpl(
    private val api: BackendService,
    private val authRepository: AuthRepository
) : UserRepository {
    private val _users: MutableMap<String, User> = HashMap()

    override suspend fun getCurrentUser(): User {
        return getUserById(authRepository.getUserId())
    }

    override suspend fun getUserById(id: String): User {
        if (!_users.containsKey(id)) {
            _users.putSynchronously(id, requestUser(id))
        }

        return _users[id]!!
    }


    private suspend fun requestUser(userId: String): User {
        val result = safeApiCall(Dispatchers.IO) {
            api.getUserDetails(
                token = authRepository.getToken(),
                userId = userId
            )
        }

        return if (result is ApiResult.Success) {
            result.value
        } else {
            User(userId, "хто_я#$userId", null, System.currentTimeMillis())
        }
    }
}