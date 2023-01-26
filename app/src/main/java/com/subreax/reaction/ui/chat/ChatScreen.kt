package com.subreax.reaction.ui.chat

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.view.ViewTreeObserver
import androidx.compose.foundation.gestures.stopScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Mood
import androidx.compose.material.icons.filled.Send
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.subreax.reaction.api.User
import com.subreax.reaction.data.chat.Message
import com.subreax.reaction.ui.components.*
import com.subreax.reaction.ui.theme.ReactionTheme
import java.lang.Integer.max

@Composable
fun ChatScreen(
    viewModel: ChatViewModel,
    onBackPressed: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val messagesListState = rememberLazyListState()

    /*LaunchedEffect(uiState.messages.size) {
        if (messagesListState.firstVisibleItemScrollOffset != 0) {
            messagesListState.stopScroll()
        }

        //messagesListState.scrollToItem(max(uiState.messages.size - 1, 0))
    }*/

    ChatScreen(
        isLoading = uiState.isLoading,
        currentUserId = viewModel.userId,
        chatTitle = uiState.chatTitle,
        avatar = uiState.avatar,
        membersCount = uiState.membersCount,
        messages = uiState.messages,
        enteredMessage = viewModel.enteredMessage,
        onEnteredMessageChanged = viewModel::updateEnteredMessage,
        onBackPressed = onBackPressed,
        messagesListState = messagesListState,
        onSendPressed = viewModel::sendMessage
    )
}

@Composable
fun ChatScreen(
    isLoading: Boolean,
    currentUserId: String,
    chatTitle: String,
    avatar: String?,
    membersCount: Int,
    messages: List<Message>,
    enteredMessage: String,
    onEnteredMessageChanged: (String) -> Unit,
    messagesListState: LazyListState,
    onBackPressed: () -> Unit = {},
    onSendPressed: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
    ) {
        MyTopAppBar(
            chatTitle = chatTitle,
            avatar = avatar,
            membersCount = membersCount,
            onBackPressed = onBackPressed
        )
        LoadingOverlay(isLoading, modifier = Modifier.weight(1.0f)) {
            MessagesList(
                currentUserId = currentUserId,
                messages = messages,
                modifier = Modifier.weight(1.0f),
                state = messagesListState
            )
        }
        MessageInputPanel(
            message = enteredMessage,
            onMessageChanged = onEnteredMessageChanged,
            onSendPressed = onSendPressed
        )
    }
}

@Composable
fun MyTopAppBar(
    chatTitle: String, avatar: String?,
    membersCount: Int,
    onBackPressed: () -> Unit
) {
    com.google.accompanist.insets.ui.TopAppBar(
        title = {
            if (avatar != null) {
                Avatar(url = avatar, size = 40.dp)
            } else {
                AvatarPlaceholder(size = 40.dp)
            }

            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(chatTitle, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                    Text(
                        "участников: $membersCount",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal
                    )
                }
            }
        },
        contentPadding = WindowInsets.statusBars.asPaddingValues(),
        navigationIcon = {
            IconButton(onClick = onBackPressed) {
                Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Go back")
            }
        }
    )
}

@Composable
fun MessagesList(
    currentUserId: String,
    messages: List<Message>,
    modifier: Modifier = Modifier,
    state: LazyListState
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(8.dp),
        //verticalArrangement = Arrangement.Bottom,
        reverseLayout = true,
        state = state
    ) {
        items(messages) { message ->
            val isMyMsg = currentUserId == message.from.id
            val author = if (isMyMsg) null else message.from.name

            val padding =
                if (isMyMsg)
                    PaddingValues(top = 8.dp, start = 70.dp)
                else
                    PaddingValues(top = 8.dp, end = 70.dp)

            val arrangement = if (isMyMsg) Arrangement.End else Arrangement.Start

            Row(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxWidth(),
                horizontalArrangement = arrangement
            ) {
                if (!isMyMsg) {
                    AutoAvatar(
                        url = message.from.avatar,
                        size = 32.dp,
                        modifier = Modifier.padding(top = 12.dp, end = 8.dp)
                    )
                }

                Message(
                    author = author,
                    content = message.content,
                    sentTime = message.sentTime,
                    //modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

@Composable
fun MessageInputPanel(
    message: String,
    onMessageChanged: (String) -> Unit,
    onSendPressed: () -> Unit
) {
    Surface {
        Row(
            modifier = Modifier
                .heightIn(32.dp)
                .padding(horizontal = 8.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { /*TODO*/ }) {
                Icon(imageVector = Icons.Filled.Mood, contentDescription = "Emoji icon")
            }
            TextField(
                value = message,
                onValueChange = onMessageChanged,
                modifier = Modifier
                    .weight(1.0f)
                    .defaultMinSize(
                        minWidth = TextFieldDefaults.MinWidth,
                        minHeight = 32.dp
                    ),
                placeholder = {
                    Text("Message")
                },
                colors = TextFieldDefaults.textFieldColors(
                    backgroundColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                shape = MaterialTheme.shapes.small
            )
            /*BasicTextField(
                value = message,
                onValueChange = onMessageChanged,
                modifier = Modifier.weight(1.0f)
            )*/
            IconButton(onClick = onSendPressed) {
                Icon(imageVector = Icons.Filled.Send, contentDescription = "Send")
            }
        }
    }
}

@Preview(uiMode = UI_MODE_NIGHT_YES, widthDp = 400, heightDp = 700, showBackground = true)
@Composable
fun ChatScreenPreview() {
    val user = User("123", "refrigerator2k", null, System.currentTimeMillis())
    val other = User("567", "-Mipe-", null, System.currentTimeMillis())
    val messages = listOf(
        Message("", other, "нармальна", System.currentTimeMillis()),
        Message("", user, "как дела", System.currentTimeMillis()),
        Message("", user, "привет", System.currentTimeMillis())
    )

    ReactionTheme {
        ChatScreen(
            isLoading = false,
            currentUserId = "123",
            chatTitle = "ChatName",
            avatar = null,
            membersCount = 6,
            messages = messages,
            enteredMessage = "",
            onEnteredMessageChanged = {},
            messagesListState = rememberLazyListState()
        )
    }
}
