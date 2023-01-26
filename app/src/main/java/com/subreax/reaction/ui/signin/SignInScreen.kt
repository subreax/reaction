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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.subreax.reaction.data.auth.impl.FakeAuthRepository
import com.subreax.reaction.ui.CustomButton
import com.subreax.reaction.ui.CustomTextField
import com.subreax.reaction.ui.theme.ReactionTheme

@Composable
fun SignInScreen(
    signInViewModel: SignInViewModel,
    onBackPressed: () -> Unit = {},
    onSignInDone: () -> Unit = {}
) {
    val usernameFocusRequester = FocusRequester()
    val signInState by signInViewModel.uiState.collectAsState()

    val isSignInDone by signInViewModel.isSignInDone.collectAsState(false)
    LaunchedEffect(isSignInDone) {
        if (isSignInDone) {
            onSignInDone()
        }
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column {
            Spacer(Modifier.statusBarsPadding())
            IconButton(
                onClick = onBackPressed,
                modifier = Modifier.padding(8.dp)
            ) {
                Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Go back")
            }

            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Enter your account")

                CustomTextField(
                    value = signInViewModel.username,
                    onValueChange = signInViewModel::updateUsername,
                    hint = "Username",
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(usernameFocusRequester),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    ),
                )
                CustomTextField(
                    value = signInViewModel.password,
                    onValueChange = signInViewModel::updatePassword,
                    hint = "Password",
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    visualTransformation = PasswordVisualTransformation()
                )

                if (signInState is SignInState.Error) {
                    CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                        Text(
                            (signInState as SignInState.Error).msg,
                            fontSize = 14.sp,
                            color = MaterialTheme.colors.error
                        )
                    }
                }

                CustomButton(
                    text = "Sign in",
                    onClick = signInViewModel::signIn,
                    modifier = Modifier
                        .align(CenterHorizontally)
                        .padding(bottom = 16.dp),
                    enabled = signInState !is SignInState.Loading
                )

                Text(
                    text = "Forgot password?",
                    modifier = Modifier
                        .align(CenterHorizontally)
                        .clickable { },
                    color = MaterialTheme.colors.primary,
                    textDecoration = TextDecoration.Underline
                )
            }
        }
    }

    /*SideEffect {
        usernameFocusRequester.requestFocus()
    }*/
}

@Preview(uiMode = UI_MODE_NIGHT_YES)
@Composable
fun SignInScreenPreview() {
    val viewModel = SignInViewModel(FakeAuthRepository())
    ReactionTheme {
        SignInScreen(viewModel)
    }
}
