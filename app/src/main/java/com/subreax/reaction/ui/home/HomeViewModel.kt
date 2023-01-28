package com.subreax.reaction.ui.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.subreax.reaction.data.chat.Chat
import com.subreax.reaction.data.chat.ChatRepository
import kotlinx.coroutines.launch

data class HomeUiState(
    val isLoading: Boolean,
    val chats: List<Chat>
)

class HomeViewModel(
    private val chatRepository: ChatRepository,
) : ViewModel() {
    var uiState by mutableStateOf(HomeUiState(true, emptyList()))
        private set

    init {
        viewModelScope.launch {
            updateChatList()
        }

        viewModelScope.launch {
            chatRepository.onMessagesChanged.collect {
                updateChatList()
            }
        }

        viewModelScope.launch {
            chatRepository.onChatsChanged.collect {
                updateChatList()
            }
        }
    }

    private suspend fun updateChatList() {
        val chats = chatRepository.getChatsList(false)
        uiState = uiState.copy(isLoading = false, chats = chats)
    }


    class Factory(
        private val chatRepository: ChatRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return HomeViewModel(chatRepository) as T
        }
    }
}