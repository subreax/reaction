package com.subreax.reaction.data

import kotlinx.coroutines.flow.Flow


interface ConnectionObserver {
    enum class Status { Connected, Disconnected }

    fun status(): Flow<Status>
}
