package com.subreax.reaction.ui.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.subreax.reaction.data.ApplicationState
import com.subreax.reaction.data.ApplicationStateSource
import com.subreax.reaction.data.chat.Chat
import com.subreax.reaction.data.chat.ChatRepository
import kotlinx.coroutines.launch

data class HomeUiState(
    val state: ApplicationState,
    val chats: List<Chat>
)

class HomeViewModel(
    private val chatRepository: ChatRepository,
    private val appStateSrc: ApplicationStateSource
) : ViewModel() {
    var uiState by mutableStateOf(HomeUiState(ApplicationState.WaitingForNetwork, emptyList()))
        private set

    init {
        viewModelScope.launch {
            chatRepository.onMessagesChanged.collect {
                getChatList()
            }
        }

        viewModelScope.launch {
            chatRepository.onChatsChanged.collect {
                getChatList()
            }
        }

        viewModelScope.launch {
            appStateSrc.state.collect { state ->
                var chatList = uiState.chats

                if (state == ApplicationState.Ready) {
                    chatList = getChatList()
                }

                uiState = HomeUiState(state, chatList)
            }
        }
    }

    fun createChat() {
        viewModelScope.launch {
            chatRepository.createChat("Chat #${System.currentTimeMillis() % 20 + 1}")
        }
    }

    private suspend fun getChatList(): List<Chat> {
        return chatRepository.getChatsList(false)
    }


    class Factory(
        private val chatRepository: ChatRepository,
        private val appStateSource: ApplicationStateSource
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return HomeViewModel(chatRepository, appStateSource) as T
        }
    }
}