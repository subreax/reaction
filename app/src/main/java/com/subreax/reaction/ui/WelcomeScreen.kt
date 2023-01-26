package com.subreax.reaction.ui

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.subreax.reaction.R
import com.subreax.reaction.Screen
import com.subreax.reaction.ui.theme.ReactionTheme

@Composable
fun WelcomeScreen(
    onClickSignIn: () -> Unit = {},
    onClickSignUp: () -> Unit = {}
) {
    Surface {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier = Modifier
                        .width(96.dp)
                        .height(96.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colors.primary)
                )

                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.h4.copy(fontWeight = FontWeight.Light),
                    modifier = Modifier.padding(top = 16.dp)
                )

                Text(text = "Ну здарова епта")
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CustomButton(
                    text = "Sign in",
                    onClick = onClickSignIn
                )
                CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                    Text(
                        text = "- OR -",
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                }
                CustomOutlinedButton(
                    text = "Sign up",
                    onClick = onClickSignUp
                )
            }
        }
    }
}

@Preview
@Preview(uiMode = UI_MODE_NIGHT_YES)
@Composable
fun WelcomeScreenPreview() {
    ReactionTheme {
        WelcomeScreen()
    }
}