package com.subreax.reaction.ui.signin

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.subreax.reaction.R
import com.subreax.reaction.StringResource
import com.subreax.reaction.api.ApiResult
import com.subreax.reaction.toStringResource
import com.subreax.reaction.data.auth.AuthRepository
import kotlinx.coroutines.launch

data class SignInUiState(
    val isSignInDone: Boolean = false,
    val isLoading: Boolean = false,
    val errorMsg: StringResource = StringResource()
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
                uiState = SignInUiState(errorMsg = R.string.err_enter_username.toStringResource())
            }
            else if (password.isEmpty()) {
                uiState = SignInUiState(errorMsg = R.string.err_enter_password.toStringResource())
            }
            else {
                uiState = SignInUiState(isLoading = true)
                val result = authRepository.signIn(AuthRepository.SignInData(username, password))
                if (result is ApiResult.Success) {
                    uiState = SignInUiState(isSignInDone = true)
                }
                else {
                    uiState = SignInUiState(errorMsg = result.errorToString().toStringResource())
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
