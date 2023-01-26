package com.subreax.reaction.ui.signup

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.subreax.reaction.api.ApiResult
import com.subreax.reaction.data.auth.AuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class SignUpViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {
    var isLoading by mutableStateOf(false)
        private set

    var message by mutableStateOf("")
        private set

    var isSignUpDone by mutableStateOf(false)
        private set

    var email by mutableStateOf("")
        private set

    var username by mutableStateOf("")
        private set

    var password by mutableStateOf("")
        private set


    fun updateEmail(email: String) {
        this.email = email.trim()
    }

    fun updateUsername(username: String) {
        this.username = username.trim()
    }

    fun updatePassword(password: String) {
        this.password = password.trim()
    }


    fun signUp() {
        viewModelScope.launch {
            if (email.isEmpty()) {
                message = "Ошибка: введите почту"
            }
            else if (username.isEmpty()) {
                message = "Ошибка: введите имя пользователя"
            }
            else if (password.isEmpty()) {
                message = "Ошибка: введите пароль"
            }
            else {
                message = ""
                isLoading = true
                if (signUpUnchecked() && signInUnchecked()) {
                    isSignUpDone = true
                }
                isLoading = false
            }
        }
    }

    private suspend fun signUpUnchecked(): Boolean {
        val result = authRepository.signUp(
            AuthRepository.SignUpData(email, username, password)
        )

        if (result is ApiResult.Success) {
            return true
        }
        message = result.errorToString()
        return false
    }

    private suspend fun signInUnchecked(): Boolean {
        val result = authRepository.signIn(
            AuthRepository.SignInData(username, password)
        )

        if (result is ApiResult.Success) {
            return true
        }
        message = result.errorToString()
        return false
    }


    class Factory(val authRepository: AuthRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SignUpViewModel(authRepository) as T
        }
    }
}
