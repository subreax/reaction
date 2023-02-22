package com.subreax.reaction.ui.signup

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.subreax.reaction.R
import com.subreax.reaction.data.auth.AuthRepository
import com.subreax.reaction.utils.Return
import com.subreax.reaction.utils.ReturnHolder
import com.subreax.reaction.utils.UiText
import kotlinx.coroutines.launch

data class SignUpUiState(
    val isLoading: Boolean = false,
    val isSignUpDone: Boolean = false,
    val errorMsg: UiText = UiText.Empty()
)


class SignUpViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {
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
                uiState = SignUpUiState(errorMsg = UiText.Res(R.string.err_enter_email))
            }
            else if (username.isEmpty()) {
                uiState = SignUpUiState(errorMsg = UiText.Res(R.string.err_enter_username))
            }
            else if (password.isEmpty()) {
                uiState = SignUpUiState(errorMsg = UiText.Res(R.string.err_enter_password))
            }
            else {
                
                uiState = SignUpUiState(isLoading = true)
                val retHolder = ReturnHolder(Return.Ok(Unit))
                
                if (signUpUnchecked(retHolder) && signInUnchecked(retHolder)) {
                    uiState = SignUpUiState(isSignUpDone = true)
                }
                else {
                    val msg = (retHolder.ret as Return.Fail).message
                    uiState = SignUpUiState(errorMsg = msg)
                }
            }
        }
    }

    private suspend fun signUpUnchecked(returnHolder: ReturnHolder<Unit>): Boolean {
        val ret = authRepository.signUp(email, username, password)

        if (ret is Return.Ok) {
            return true
        }
        returnHolder.ret = ret
        return false
    }

    private suspend fun signInUnchecked(returnHolder: ReturnHolder<Unit>): Boolean {
        val ret = authRepository.signIn(username, password)

        if (ret is Return.Ok) {
            return true
        }
        returnHolder.ret = ret
        return false
    }


    class Factory(val authRepository: AuthRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SignUpViewModel(authRepository) as T
        }
    }
}
