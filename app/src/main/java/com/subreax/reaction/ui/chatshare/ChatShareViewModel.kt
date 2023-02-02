package com.subreax.reaction.ui.chatshare

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.zxing.BarcodeFormat
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.QRCodeWriter
import com.subreax.reaction.data.chat.ChatRepository
import kotlinx.coroutines.launch

data class ChatShareUiState(
    val chatName: String,
    val qr: BitMatrix
)

class ChatShareViewModel(
    chatId: String,
    private val chatRepository: ChatRepository
) : ViewModel() {
    var uiState: ChatShareUiState? by mutableStateOf(null)
        private set

    init {
        viewModelScope.launch {
            val qr = QRCodeWriter().encode(
                "reaction://join/$chatId",
                BarcodeFormat.QR_CODE,
                0, 0
            )

            val chat = chatRepository.getChatById(chatId)!!

            uiState = ChatShareUiState(
                chatName = chat.title,
                qr = qr
            )
        }
    }

    class Factory(private val chatId: String, private val chatRepository: ChatRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ChatShareViewModel(chatId, chatRepository) as T
        }
    }
}