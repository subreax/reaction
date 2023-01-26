package com.subreax.reaction.data.user

import com.subreax.reaction.api.User

interface UserRepository {
    suspend fun getCurrentUser(): User
    suspend fun getUserById(id: String): User?
}