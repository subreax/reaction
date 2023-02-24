package com.subreax.reaction.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.subreax.reaction.R
import com.subreax.reaction.api.User
import com.subreax.reaction.data.ApplicationState
import com.subreax.reaction.data.chat.Chat
import com.subreax.reaction.data.chat.Message
import com.subreax.reaction.ui.components.ChatListItem
import com.subreax.reaction.ui.components.LoadingOverlay

@Composable
fun HomeScreen(
    //statusBarHeight: Dp,
    viewModel: HomeViewModel,
    onChatClicked: (Chat) -> Unit = {}
) {
    val uiState = viewModel.uiState

    Scaffold(
        topBar = {
            com.google.accompanist.insets.ui.TopAppBar(
                title = { AppBarTitle(state = uiState.state) },
                contentPadding = WindowInsets.statusBars.asPaddingValues(),
                navigationIcon = {
                    IconButton(onClick = { /*TODO*/ }) {
                        Icon(Icons.Filled.Menu, contentDescription = "Menu")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = viewModel::createChat, modifier = Modifier.navigationBarsPadding()) {
                Icon(imageVector = Icons.Filled.Edit, contentDescription = "Create a chat")
            }
        }
    ) { innerPadding ->
        LoadingOverlay(isLoading = uiState.state != ApplicationState.Ready && uiState.chats.isEmpty()) {
            if (uiState.chats.isEmpty()) {
                NoChatsLabel()
            }
            else {
                ChatList(
                    chats = uiState.chats,
                    modifier = Modifier.padding(innerPadding),
                    onChatClicked = onChatClicked
                )
            }
        }
    }
}

@Composable
fun AppBarTitle(state: ApplicationState) {
    val text = when (state) {
        ApplicationState.WaitingForNetwork -> stringResource(R.string.waiting_for_network)
        ApplicationState.Connecting -> stringResource(R.string.connecting)
        ApplicationState.Syncing -> stringResource(R.string.syncing)
        ApplicationState.Ready -> stringResource(R.string.app_name)
    }

    Text(text)
}

@Composable
private fun NoChatsLabel() {
    Box(Modifier.fillMaxSize()) {
        Text("Пусто", modifier = Modifier.align(Alignment.Center))
    }
}


@Composable
fun ChatList(
    chats: List<Chat>,
    modifier: Modifier = Modifier,
    onChatClicked: (Chat) -> Unit = {}
) {
    val navBarsPadding = with(LocalDensity.current) {
        (WindowInsets.navigationBars.getBottom(this) + WindowInsets.navigationBars.getTop(this)).toDp()
    }

    LazyColumn(modifier = modifier, contentPadding = PaddingValues(bottom = navBarsPadding)) {
        items(chats) { chat ->
            ChatListItem(
                chatId = chat.id,
                chatName = chat.title,
                avatar = chat.avatar,
                lastMessage = chat.lastMessage ?: Message(
                    chatId = "",
                    from = User(
                        "",
                        "NoUser",
                        null,
                        System.currentTimeMillis()
                    ),
                    content = "NoContent",
                    sentTime = System.currentTimeMillis()
                ),
                unreadMessagesCount = 0,
                isMuted = chat.isMuted,
                isPinned = chat.isPinned,
                modifier = Modifier.clickable { onChatClicked(chat) }
            )

            Divider(modifier = Modifier.padding(start = 72.dp))
        }
    }
}
