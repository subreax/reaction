package com.subreax.reaction.ui.chateditor

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.subreax.reaction.data.chat.ChatRepository
import kotlinx.coroutines.launch

class ChatEditorViewModel(
    val chatId: String,
    private val chatRepository: ChatRepository,
    private val navigateBack: () -> Unit
) : ViewModel() {
    var chatName by mutableStateOf("")
        private set

    var chatAvatar: String? by mutableStateOf(null)
        private set

    var showWarningDialog by mutableStateOf(false)
        private set

    private lateinit var initialChatName: String


    init {
        viewModelScope.launch {
            val chat = chatRepository.getChatById(chatId)!!
            chatName = chat.title
            initialChatName = chat.title
        }
    }

    fun updateChatName(name: String) {
        chatName = name
    }

    fun applyChanges() {
        if (hasChanges()) {
            viewModelScope.launch {
                chatRepository.setChatName(chatId, chatName)
                navigateBack()
            }
        }
        else {
            navigateBack()
        }
    }

    fun dismissWarningDialog() {
        showWarningDialog = false
    }

    fun discardChanges() {
        navigateBack()
    }

    fun navBack() {
        if (hasChanges()) {
            showWarningDialog = true
        }
        else {
            navigateBack()
        }
    }

    private fun hasChanges() = chatName != initialChatName



    class Factory(
        val chatId: String,
        private val chatRepository: ChatRepository,
        private val navigateBack: () -> Unit,
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ChatEditorViewModel(chatId, chatRepository, navigateBack) as T
        }
    }
}