package com.subreax.reaction.ui.signup

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

data class SignUpUiState(
    val isLoading: Boolean = false,
    val isSignUpDone: Boolean = false,
    val errorMsg: StringResource = StringResource()
)


class SignUpViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {
    private data class StringContainer(var string: String) {
        fun toStringResource() = string.toStringResource()
    }

    var uiState by mutableStateOf(SignUpUiState())
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
                uiState = SignUpUiState(errorMsg = R.string.err_enter_email.toStringResource())
            }
            else if (username.isEmpty()) {
                uiState = SignUpUiState(errorMsg = R.string.err_enter_username.toStringResource())
            }
            else if (password.isEmpty()) {
                uiState = SignUpUiState(errorMsg = R.string.err_enter_password.toStringResource())
            }
            else {
                val errorMsg = StringContainer("")
                uiState = SignUpUiState(isLoading = true)
                if (signUpUnchecked(errorMsg) && signInUnchecked(errorMsg)) {
                    uiState = SignUpUiState(isSignUpDone = true)
                }
                else {
                    uiState = SignUpUiState(errorMsg = errorMsg.toStringResource())
                }
            }
        }
    }

    private suspend fun signUpUnchecked(outError: StringContainer): Boolean {
        val result = authRepository.signUp(
            AuthRepository.SignUpData(email, username, password)
        )

        if (result is ApiResult.Success) {
            return true
        }
        outError.string = result.errorToString()
        return false
    }

    private suspend fun signInUnchecked(outError: StringContainer): Boolean {
        val result = authRepository.signIn(
            AuthRepository.SignInData(username, password)
        )

        if (result is ApiResult.Success) {
            return true
        }
        outError.string = result.errorToString()
        return false
    }


    class Factory(val authRepository: AuthRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SignUpViewModel(authRepository) as T
        }
    }
}
