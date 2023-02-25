package com.subreax.reaction.ui.chatdetails

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Share
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.accompanist.insets.ui.TopAppBar
import com.subreax.reaction.R
import com.subreax.reaction.api.User
import com.subreax.reaction.ui.LocalStatusBarPadding
import com.subreax.reaction.ui.components.AutoAvatar
import com.subreax.reaction.ui.components.OptionItem
import com.subreax.reaction.ui.theme.ReactionTheme
import com.subreax.reaction.utils.pluralResource

@Composable
fun ChatDetailsScreen(
    chatDetailsViewModel: ChatDetailsViewModel
) {
    val uiState = chatDetailsViewModel.uiState

    ChatDetailsScreen(
        chatId = uiState.chatId,
        chatName = uiState.chatName,
        members = uiState.members,
        onEditClicked = chatDetailsViewModel::editChat,
        onShareClicked = chatDetailsViewModel::shareChat,
        onBackClicked = chatDetailsViewModel::navigateBack,
        isNotificationsEnabled = chatDetailsViewModel.isNotificationsEnabled,
        onNotificationsToggled = chatDetailsViewModel::toggleNotifications
    )
}

@Composable
fun ChatDetailsScreen(
    chatId: String,
    chatName: String,
    members: List<User>,
    onEditClicked: () -> Unit,
    onShareClicked: () -> Unit,
    onBackClicked: () -> Unit,
    isNotificationsEnabled: Boolean,
    onNotificationsToggled: (Boolean) -> Unit,
) {
    Column {
        TopAppBar(
            onEditClicked = onEditClicked,
            onBackClicked = onBackClicked
        )
        ChatGeneralInfo(chatId, chatName, members.size)

        Surface {
            Column {
                OptionItem(
                    icon = Icons.Filled.Notifications,
                    title = stringResource(R.string.notifications),
                    onClick = { onNotificationsToggled(!isNotificationsEnabled) },
                    contentPadding = edgePadding
                ) {
                    Switch(
                        checked = isNotificationsEnabled,
                        onCheckedChange = onNotificationsToggled
                    )
                }
                OptionItem(
                    icon = Icons.Filled.Share,
                    title = stringResource(R.string.share),
                    onClick = onShareClicked,
                    contentPadding = edgePadding
                )
            }
        }

        MembersList(members)
    }
}

@Composable
private fun TopAppBar(onEditClicked: () -> Unit, onBackClicked: () -> Unit) {
    TopAppBar(
        contentPadding = LocalStatusBarPadding.current,
        navigationIcon = {
            IconButton(onClick = onBackClicked) {
                Icon(Icons.Filled.ArrowBack, stringResource(R.string.nav_back))
            }
        },
        actions = {
            IconButton(onClick = onEditClicked) {
                Icon(Icons.Filled.Edit, stringResource(R.string.edit_chat_name))
            }
        },
        title = {},
    )
}

@Composable
private fun ChatGeneralInfo(
    chatId: String,
    chatName: String,
    membersCount: Int
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        elevation = AppBarDefaults.TopAppBarElevation
    ) {
        Row(
            modifier = Modifier.padding(edgePadding).padding(vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AutoAvatar(
                colorStr = chatId,
                title = chatName,
                url = null,
                size = 64.dp,
                modifier = Modifier.padding(end = 16.dp)
            )
            Column {
                Text(
                    text = chatName,
                    style = MaterialTheme.typography.h6,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = pluralResource(R.plurals.members_count, membersCount, membersCount),
                    color = LocalContentColor.current.copy(alpha = ContentAlpha.medium)
                )
            }
        }
    }
}

@Composable
private fun MembersList(members: List<User>) {
    Surface(
        Modifier
            .padding(top = 16.dp)
            .fillMaxSize()
    ) {
        Column(Modifier.padding(edgePadding)) {
            Text(
                text = stringResource(R.string.members),
                modifier = Modifier.padding(vertical = 8.dp),
                color = MaterialTheme.colors.primary,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                for (user in members) {
                    MemberListItem(userId = user.id, username = user.name, avatar = user.avatar)
                }
            }
        }
    }
}

@Composable
private fun MemberListItem(userId: String, username: String, avatar: String?) {
    Row(
        modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically
    ) {
        AutoAvatar(
            colorStr = userId,
            title = username,
            url = avatar,
            size = 48.dp,
            modifier = Modifier.padding(end = 12.dp)
        )
        Column {
            Text(
                text = username,
                style = MaterialTheme.typography.body1.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                text = "Был в сети когда-то",
                style = MaterialTheme.typography.body2,
                color = LocalContentColor.current.copy(alpha = ContentAlpha.medium)
            )
        }
    }
}

@Preview(uiMode = UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun ChatDetailsScreenPreview() {
    ReactionTheme {
        ChatDetailsScreen(
            chatId = "",
            chatName = "Chat Name",
            members = listOf(
                User("", "Хто я", null, 0),
                User("", "Кто-то", null, 0)
            ),
            onEditClicked = {},
            onShareClicked = {},
            onBackClicked = {},
            isNotificationsEnabled = true,
            onNotificationsToggled = {}
        )
    }
}


private val edgePadding = PaddingValues(horizontal = 16.dp)