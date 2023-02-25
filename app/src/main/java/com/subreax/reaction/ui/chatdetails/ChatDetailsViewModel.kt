package com.subreax.reaction.ui.chatdetails

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.subreax.reaction.api.User
import com.subreax.reaction.data.chat.ChatRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class ChatDetailsUiState(
    val chatId: String,
    val chatName: String,
    val members: List<User>
)

class ChatDetailsViewModel(
    private val chatId: String,
    private val navBack: () -> Unit,
    private val navToChatSharing: () -> Unit,
    private val navToChatEditor: () -> Unit,
    private val chatRepository: ChatRepository
) : ViewModel() {
    var uiState by mutableStateOf(ChatDetailsUiState(chatId, "", emptyList()))

    var isNotificationsEnabled by mutableStateOf(false)
        private set

    private var toggleNotificationJob: Job? = null

    init {
        requestChatDetails()
        listenForChatChanges()
    }

    fun toggleNotifications(enabled: Boolean) {
        isNotificationsEnabled = enabled
        toggleNotificationJob?.cancel()
        toggleNotificationJob = viewModelScope.launch {
            delay(200)
            chatRepository.toggleNotifications(chatId, enabled)
        }
    }

    fun navigateBack() {
        navBack()
    }

    fun shareChat() {
        navToChatSharing()
    }

    fun editChat() {
        navToChatEditor()
    }

    private fun requestChatDetails() {
        viewModelScope.launch {
            val chat = chatRepository.getChatById(chatId)
            if (chat == null) {
                Log.e("ChatDetailsVM", "Chat is null. id: $chatId")
                return@launch
            }

            uiState = ChatDetailsUiState(
                chatId,
                chat.title,
                chatRepository.getChatMembers(chatId)
            )
            isNotificationsEnabled = !chat.isMuted
        }
    }

    private fun listenForChatChanges() {
        viewModelScope.launch {
            chatRepository.onChatChanged.collect {
                if (it == chatId || it.isEmpty()) {
                    requestChatDetails()
                }
            }
        }
    }

    class Factory(
        private val chatId: String,
        private val navBack: () -> Unit,
        private val navToChatSharing: () -> Unit,
        private val navToChatEditor: () -> Unit,
        private val chatRepository: ChatRepository,
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ChatDetailsViewModel(
                chatId,
                navBack,
                navToChatSharing,
                navToChatEditor,
                chatRepository
            ) as T
        }
    }
}