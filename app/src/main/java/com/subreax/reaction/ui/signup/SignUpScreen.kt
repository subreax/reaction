package com.subreax.reaction.ui.signup

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.subreax.reaction.ui.CustomButton
import com.subreax.reaction.ui.CustomTextField
import com.subreax.reaction.ui.signin.SignInState

@Composable
fun SignUpScreen(
    signUpViewModel: SignUpViewModel,
    onBackPressed: () -> Unit = {},
    onSignUpDone: () -> Unit = {}
) {
    val message = signUpViewModel.message

    LaunchedEffect(signUpViewModel.isSignUpDone) {
        if (signUpViewModel.isSignUpDone) {
            onSignUpDone()
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
                Text("To get started, заполни вот эту ксиву")

                CustomTextField(
                    value = signUpViewModel.email,
                    onValueChange = signUpViewModel::updateEmail,
                    hint = "E-mail",
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    ),
                )

                CustomTextField(
                    value = signUpViewModel.username,
                    onValueChange = signUpViewModel::updateUsername,
                    hint = "Username",
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    ),
                )

                CustomTextField(
                    value = signUpViewModel.password,
                    onValueChange = signUpViewModel::updatePassword,
                    hint = "Password",
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    visualTransformation = PasswordVisualTransformation()
                )

                if (message.isNotEmpty()) {
                    CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                        Text(message, fontSize = 14.sp, color = MaterialTheme.colors.error)
                    }
                }

                Text(text = "Нажимая Sign up, вы соглашаетесь на оформление кредита", fontSize = 6.sp)

                CustomButton(
                    text = "Sign up",
                    onClick = signUpViewModel::signUp,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(bottom = 16.dp),
                    enabled = !signUpViewModel.isLoading
                )
            }
        }
    }
}