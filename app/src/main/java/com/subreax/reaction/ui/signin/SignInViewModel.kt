package com.subreax.reaction.ui.signin

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.subreax.reaction.api.ApiResult
import com.subreax.reaction.data.auth.AuthRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class SignInState {
    object Nothing : SignInState()
    object Loading : SignInState()
    object Success : SignInState()
    data class Error(val msg: String) : SignInState()
}

class SignInViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow<SignInState>(SignInState.Nothing)
    val uiState = _uiState.asStateFlow()

    var username by mutableStateOf("")
        private set

    var password by mutableStateOf("")
        private set

    private val _isSignInDone = MutableSharedFlow<Boolean>()
    val isSignInDone: SharedFlow<Boolean>
        get() = _isSignInDone

    fun updateUsername(username: String) {
        this.username = username.trim()
    }

    fun updatePassword(password: String) {
        this.password = password.trim()
    }

    fun signIn() {
        viewModelScope.launch {
            if (username.isEmpty()) {
                _uiState.value = SignInState.Error("Ошибка: введите имя пользователя")
            }
            else if (password.isEmpty()) {
                _uiState.value = SignInState.Error("Ошибка: введите пароль")
            }
            else {
                _uiState.value = SignInState.Loading
                val result = authRepository.signIn(AuthRepository.SignInData(username, password))
                if (result is ApiResult.Success) {
                    _uiState.value = SignInState.Success
                    _isSignInDone.emit(true)
                }
                else {
                    _uiState.value = SignInState.Error(result.errorToString())
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
