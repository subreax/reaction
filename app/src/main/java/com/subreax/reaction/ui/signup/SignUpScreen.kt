package com.subreax.reaction.ui.signup

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.subreax.reaction.R
import com.subreax.reaction.ui.CustomButton
import com.subreax.reaction.ui.CustomTextField
import com.subreax.reaction.ui.theme.ReactionTheme

@Composable
fun SignUpScreen(
    signUpViewModel: SignUpViewModel,
    onBackPressed: () -> Unit = {},
    onSignUpDone: () -> Unit = {}
) {
    val uiState = signUpViewModel.uiState

    LaunchedEffect(uiState.isSignUpDone) {
        if (uiState.isSignUpDone) {
            onSignUpDone()
        }
    }

    SignUpScreen(
        email = signUpViewModel.email,
        username = signUpViewModel.username,
        password = signUpViewModel.password,
        onEmailChanged = signUpViewModel::updateEmail,
        onUsernameChanged = signUpViewModel::updateUsername,
        onPasswordChanged = signUpViewModel::updatePassword,
        onSignUpClicked = signUpViewModel::signUp,
        onBackPressed = onBackPressed,
        isLoading = uiState.isLoading,
        errorMsg = uiState.errorMsg.value
    )
}

@Composable
fun SignUpScreen(
    email: String,
    username: String,
    password: String,
    onEmailChanged: (String) -> Unit,
    onUsernameChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onSignUpClicked: () -> Unit,
    onBackPressed: () -> Unit,
    isLoading: Boolean,
    errorMsg: String
) {
    val emailFocusRequester = FocusRequester()
    LaunchedEffect(Unit) {
        emailFocusRequester.requestFocus()
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column {
            Spacer(Modifier.statusBarsPadding())
            IconButton(
                onClick = onBackPressed,
                modifier = Modifier.padding(8.dp)
            ) {
                Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = stringResource(R.string.nav_back))
            }

            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(stringResource(R.string.sign_up_enter_account))

                CustomTextField(
                    value = email,
                    onValueChange = onEmailChanged,
                    hint = stringResource(R.string.email),
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(emailFocusRequester),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    ),
                )

                CustomTextField(
                    value = username,
                    onValueChange = onUsernameChanged,
                    hint = stringResource(R.string.username),
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    ),
                )

                CustomTextField(
                    value = password,
                    onValueChange = onPasswordChanged,
                    hint = stringResource(R.string.password),
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    visualTransformation = PasswordVisualTransformation()
                )

                if (errorMsg.isNotEmpty()) {
                    Text(
                        errorMsg,
                        fontSize = 14.sp,
                        color = MaterialTheme.colors.error.copy(alpha = ContentAlpha.medium)
                    )
                }

                CustomButton(
                    text = stringResource(R.string.sign_up),
                    onClick = onSignUpClicked,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(bottom = 16.dp),
                    enabled = !isLoading
                )
            }
        }
    }
}

@Preview(uiMode = UI_MODE_NIGHT_YES)
@Composable
fun SignUpPreview() {
    ReactionTheme {
        SignUpScreen(
            email = "",
            username = "",
            password = "",
            onEmailChanged = {},
            onUsernameChanged = {},
            onPasswordChanged = {},
            onSignUpClicked = {  },
            onBackPressed = {  },
            isLoading = false,
            errorMsg = ""
        )
    }
}
