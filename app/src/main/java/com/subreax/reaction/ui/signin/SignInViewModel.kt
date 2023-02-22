package com.subreax.reaction.ui.signin

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.subreax.reaction.R
import com.subreax.reaction.data.auth.AuthRepository
import com.subreax.reaction.utils.Return
import com.subreax.reaction.utils.UiText
import kotlinx.coroutines.launch

data class SignInUiState(
    val isSignInDone: Boolean = false,
    val isLoading: Boolean = false,
    val errorMsg: UiText = UiText.Empty()
)

class SignInViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {
    var uiState by mutableStateOf(SignInUiState())
        private set

    var username by mutableStateOf("")
        private set

    var password by mutableStateOf("")
        private set

    fun updateUsername(username: String) {
        this.username = username.trim()
    }

    fun updatePassword(password: String) {
        this.password = password.trim()
    }

    fun signIn() {
        viewModelScope.launch {
            if (username.isEmpty()) {
                uiState = SignInUiState(errorMsg = UiText.Res(R.string.err_enter_username))
            }
            else if (password.isEmpty()) {
                uiState = SignInUiState(errorMsg = UiText.Res(R.string.err_enter_password))
            }
            else {
                uiState = SignInUiState(isLoading = true)
                val ret = authRepository.signIn(username, password)
                uiState = when (ret) {
                    is Return.Ok -> SignInUiState(isSignInDone = true)
                    is Return.Fail -> SignInUiState(errorMsg = ret.message)
                }
            }
        }
    }

    class Factory(val authRepository: AuthRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SignInViewModel(authRepository) as T
        }
    }
}
