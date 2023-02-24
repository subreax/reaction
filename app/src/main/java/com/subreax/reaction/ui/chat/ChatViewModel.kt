package com.subreax.reaction.ui.chat

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.subreax.reaction.data.ApplicationState
import com.subreax.reaction.data.ApplicationStateSource
import com.subreax.reaction.data.chat.ChatRepository
import com.subreax.reaction.data.chat.Message
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class ChatUiState(
    val isLoading: Boolean,
    val chatId: String,
    val title: String,
    val avatar: String?,
    val membersCount: Int,
    val appState: ApplicationState,
    val messages: List<Message> = emptyList(),
    val navBack: Boolean = false
)

class ChatViewModel(
    val userId: String,
    private val chatId: String,
    private val navToDetailsScreen: () -> Unit,
    private val chatRepository: ChatRepository,
    private val applicationStateSource: ApplicationStateSource
) : ViewModel() {
    private val _uiState = MutableStateFlow(ChatUiState(
        true,
        chatId,
        "",
        null,
        0,
        ApplicationState.WaitingForNetwork,
        emptyList())
    )

    val uiState: StateFlow<ChatUiState>
        get() = _uiState

    var enteredMessage by mutableStateOf("")
        private set

    init {
        applicationStateSource.addSyncingAction(this::syncMessages)

        viewModelScope.launch {
            applicationStateSource.state.collect { appState ->
                _uiState.value = _uiState.value.copy(appState = appState)
            }
        }

        viewModelScope.launch {
            val chat = chatRepository.getChatById(chatId)
            if (chat != null) {
                _uiState.value = _uiState.value.copy(
                    title = chat.title,
                    avatar = chat.avatar,
                    membersCount = chat.membersCount
                )
                syncMessages()
            }
            else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    title = "Unknown chat"
                )
            }
        }

        listenMessagesChanging()
    }

    private suspend fun syncMessages(): Boolean {
        val chat = chatRepository.getChatById(chatId)
        if (chat != null) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                messages = chatRepository.getMessages(chatId)
            )
        }
        return true
    }

    private fun listenMessagesChanging() {
        viewModelScope.launch {
            chatRepository.onMessagesChanged.collect { changedChat ->
                if (changedChat.id == chatId) {
                    _uiState.value = _uiState.value.copy(
                        messages = chatRepository.getMessages(chatId)
                    )
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        applicationStateSource.removeSyncingAction(this::syncMessages)
    }

    fun updateEnteredMessage(message: String) {
        enteredMessage = message
    }

    fun sendMessage() {
        viewModelScope.launch {
            val msg = enteredMessage.trim()
            if (msg.isNotEmpty()) {
                chatRepository.sendMessage(chatId, enteredMessage.trim())
                enteredMessage = ""
            }
        }
    }

    fun navigateToDetailsScreen() {
        navToDetailsScreen()
    }

    fun leaveChat() {
        viewModelScope.launch {
            chatRepository.leaveChat(chatId)
            navBack()
        }
    }

    private fun navBack() {
        _uiState.value = _uiState.value.copy(navBack = true)
    }

    class Factory(
        private val userId: String,
        private val chatId: String,
        private val navToDetailsScreen: () -> Unit,
        private val chatRepository: ChatRepository,
        private val applicationStateSource: ApplicationStateSource
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ChatViewModel(userId, chatId, navToDetailsScreen, chatRepository, applicationStateSource) as T
        }
    }
}