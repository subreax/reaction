package com.subreax.reaction.ui.joinchat

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.subreax.reaction.ui.components.CustomButton
import com.subreax.reaction.ui.components.CustomOutlinedButton
import com.subreax.reaction.ui.components.AutoAvatar
import com.subreax.reaction.ui.components.LoadingOverlay
import com.subreax.reaction.ui.theme.ReactionTheme

@Composable
fun JoinChatScreen(
    joinChatViewModel: JoinChatViewModel
) {
    val uiState = joinChatViewModel.uiState

    when (uiState) {
        is JoinScreenUiState.Data -> {
            JoinChatScreen(
                chatId = uiState.chatId,
                name = uiState.chatName,
                avatar = uiState.avatar,
                membersCount = uiState.membersCount,
                joinClicked = joinChatViewModel::joinChat,
                cancelClicked = joinChatViewModel::cancelInvite,
                isJoining = uiState.isJoining
            )
        }

        is JoinScreenUiState.Loading -> {
            LoadingOverlay(isLoading = true)
        }

        is JoinScreenUiState.Error -> {
            Surface {
                Box(Modifier.fillMaxSize()) {
                    Text(
                        text = uiState.msg,
                        modifier = Modifier
                            .padding(16.dp)
                            .align(Alignment.Center)
                    )
                }
            }
        }
    }
}

@Composable
fun JoinChatScreen(
    chatId: String,
    name: String,
    avatar: String?,
    membersCount: Int,
    joinClicked: () -> Unit,
    cancelClicked: () -> Unit,
    isJoining: Boolean
) {
    Surface(Modifier.fillMaxSize()) {
        Column(
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                AutoAvatar(colorStr = chatId, name, avatar, size = 96.dp)
                Text(
                    text = name,
                    style = MaterialTheme.typography.h4.copy(fontWeight = FontWeight.Light),
                    modifier = Modifier.padding(top = 32.dp)
                )
                Text(
                    text = "Участников: $membersCount",
                    color = LocalContentColor.current.copy(alpha = ContentAlpha.medium),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CustomButton(
                    text = if (isJoining) "Вход..." else "Вступить",
                    onClick = joinClicked,
                    enabled = !isJoining
                )
                CustomOutlinedButton(
                    text = "Отмена",
                    onClick = cancelClicked,
                    enabled = !isJoining
                )
            }
        }
    }
}

@Preview(uiMode = UI_MODE_NIGHT_YES, widthDp = 320, heightDp = 640)
@Composable
fun JoinChatScreenPreview() {
    ReactionTheme {
        JoinChatScreen(
            chatId = "",
            name = "ChatName",
            avatar = null,
            membersCount = 12,
            joinClicked = { },
            cancelClicked = { },
            isJoining = false
        )
    }
}