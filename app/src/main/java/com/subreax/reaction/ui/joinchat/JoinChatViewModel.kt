package com.subreax.reaction.ui.joinchat

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.subreax.reaction.data.chat.ChatRepository
import kotlinx.coroutines.launch

sealed class JoinScreenUiState {
    object Loading : JoinScreenUiState()
    class Error(val msg: String) : JoinScreenUiState()
    data class Data(
        val chatId: String,
        val chatName: String,
        val avatar: String?,
        val membersCount: Int,
        val isJoining: Boolean
    ) : JoinScreenUiState()
}

class JoinChatViewModel(
    private val chatId: String,
    private val chatRepository: ChatRepository,
    private val navHome: () -> Unit,
    private val navToChat: (chatId: String) -> Unit
) : ViewModel() {
    var uiState by mutableStateOf<JoinScreenUiState>(JoinScreenUiState.Loading)
        private set

    init {
        viewModelScope.launch {
            if (!chatRepository.isUserAMemberOfTheChat(chatId)) {
                val chat = chatRepository.getChatById(chatId)
                Log.d("JoinChatViewModel", "Chat name: ${chat?.title}")
                if (chat != null) {
                    uiState = JoinScreenUiState.Data(
                        chatId = chat.id,
                        chatName = chat.title,
                        avatar = chat.avatar,
                        membersCount = chat.membersCount,
                        isJoining = false
                    )
                } else {
                    uiState = JoinScreenUiState.Error("Вас пригласили в несуществующий чат")
                }
            }
            else {
                uiState = JoinScreenUiState.Error("Вы уже состоите в этом чате")
            }
        }
    }

    fun joinChat() {
        if (uiState is JoinScreenUiState.Data) {
            viewModelScope.launch {
                uiState = (uiState as JoinScreenUiState.Data).copy(
                    isJoining = true
                )
                chatRepository.joinChat(chatId)
                navToChat(chatId)
            }
        }
    }

    fun cancelInvite() {
        navHome()
    }


    class Factory(
        private val chatId: String,
        private val chatRepository: ChatRepository,
        private val navHome: () -> Unit,
        private val navToChat: (chatId: String) -> Unit
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return JoinChatViewModel(chatId, chatRepository, navHome, navToChat) as T
        }
    }
}
