package com.subreax.reaction.ui.chat

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.subreax.reaction.data.chat.ChatRepository
import com.subreax.reaction.data.chat.Message
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class ChatUiState(
    val isLoading: Boolean,
    val chatTitle: String,
    val avatar: String?,
    val membersCount: Int,
    val messages: List<Message>
)

class ChatViewModel(
    val userId: String,
    private val chatId: String,
    private val chatRepository: ChatRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(ChatUiState(true, "", null, 0, emptyList()))
    val uiState: StateFlow<ChatUiState>
        get() = _uiState


    var enteredMessage by mutableStateOf("")
        private set

    init {
        viewModelScope.launch {
            val chat = withContext(Dispatchers.IO) {
                chatRepository.getChatById(chatId)
            }

            if (chat != null) {
                _uiState.value = ChatUiState(
                    true,
                    chat.title,
                    chat.avatar,
                    chat.membersCount,
                    emptyList()
                )

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    messages = getMessages()
                )
            }
            else {
                Log.e("ChatViewModel", "Unknown chat: $chatId")
                _uiState.value = ChatUiState(
                    isLoading = false,
                    "unknown chat",
                    null,
                    0,
                    emptyList()
                )
            }

            chatRepository.onMessagesChanged.collect { changedChat ->
                if (changedChat.id == chatId) {
                    _uiState.value = _uiState.value.copy(
                        messages = getMessages()
                    )
                }
            }
        }
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

    private suspend fun getMessages(): List<Message> {
        return withContext(Dispatchers.IO) {
            chatRepository.getMessages(chatId)
        }
    }


    class Factory(
        private val userId: String,
        private val chatId: String,
        private val chatRepository: ChatRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ChatViewModel(userId, chatId, chatRepository) as T
        }
    }
}