package com.subreax.reaction.ui

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.subreax.reaction.R
import com.subreax.reaction.ui.components.CustomButton
import com.subreax.reaction.ui.components.CustomOutlinedButton
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

                Text(
                    text = stringResource(R.string.welcome),
                    color = LocalContentColor.current.copy(alpha = ContentAlpha.medium)
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CustomButton(
                    text = stringResource(R.string.sign_in),
                    onClick = onClickSignIn
                )

                Text(
                    text = stringResource(R.string.or),
                    modifier = Modifier.padding(vertical = 16.dp),
                    color = LocalContentColor.current.copy(alpha = ContentAlpha.medium)
                )

                CustomOutlinedButton(
                    text = stringResource(R.string.sign_up),
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