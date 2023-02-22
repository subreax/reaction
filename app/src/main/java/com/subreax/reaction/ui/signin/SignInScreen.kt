package com.subreax.reaction.ui.signin

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.subreax.reaction.R
import com.subreax.reaction.ui.CustomButton
import com.subreax.reaction.ui.CustomTextField
import com.subreax.reaction.ui.theme.ReactionTheme

@Composable
fun SignInScreen(
    signInViewModel: SignInViewModel,
    onBackPressed: () -> Unit = {},
    onSignInDone: () -> Unit = {}
) {
    val uiState = signInViewModel.uiState
    
    LaunchedEffect(uiState.isSignInDone) {
        if (uiState.isSignInDone) {
            onSignInDone()
        }
    }

    SignInScreen(
        username = signInViewModel.username,
        password = signInViewModel.password,
        onUsernameChanged = signInViewModel::updateUsername,
        onPasswordChanged = signInViewModel::updatePassword,
        onSignInClicked = signInViewModel::signIn,
        onBackPressed = onBackPressed,
        isLoading = uiState.isLoading,
        errorMsg = uiState.errorMsg.asString()
    )
}

@Composable
fun SignInScreen(
    username: String,
    password: String,
    onUsernameChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onSignInClicked: () -> Unit,
    onBackPressed: () -> Unit,
    isLoading: Boolean,
    errorMsg: String
) {
    val usernameFocusRequester = FocusRequester()
    LaunchedEffect(Unit) {
        usernameFocusRequester.requestFocus()
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
                Text(stringResource(R.string.enter_your_account))

                CustomTextField(
                    value = username,
                    onValueChange = onUsernameChanged,
                    hint = stringResource(R.string.username),
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(usernameFocusRequester),
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
                        text = errorMsg,
                        fontSize = 14.sp,
                        color = MaterialTheme.colors.error.copy(alpha = ContentAlpha.medium)
                    )
                }

                CustomButton(
                    text = stringResource(id = R.string.sign_in),
                    onClick = onSignInClicked,
                    modifier = Modifier
                        .align(CenterHorizontally)
                        .padding(bottom = 16.dp),
                    enabled = !isLoading
                )

                Text(
                    text = stringResource(R.string.forgot_password),
                    modifier = Modifier
                        .align(CenterHorizontally)
                        .clickable { },
                    color = MaterialTheme.colors.primary,
                    textDecoration = TextDecoration.Underline
                )
            }
        }
    }
}

@Preview(uiMode = UI_MODE_NIGHT_YES)
@Composable
fun SignInScreenPreview() {
    ReactionTheme {
        SignInScreen(
            username = "",
            password = "",
            onUsernameChanged = {},
            onPasswordChanged = {},
            onSignInClicked = {},
            onBackPressed = {},
            isLoading = false,
            errorMsg = ""
        )
    }
}
