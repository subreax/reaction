package com.subreax.reaction.ui.chat

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.subreax.reaction.R
import com.subreax.reaction.api.User
import com.subreax.reaction.data.chat.Message
import com.subreax.reaction.ui.components.AutoAvatar
import com.subreax.reaction.ui.components.LoadingOverlay
import com.subreax.reaction.ui.components.Message
import com.subreax.reaction.ui.theme.ReactionTheme

@Composable
fun ChatScreen(
    viewModel: ChatViewModel,
    onBackPressed: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val messagesListState = rememberLazyListState()

    LaunchedEffect(uiState.navBack) {
        if (uiState.navBack) {
            onBackPressed()
        }
    }

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
        onSendPressed = viewModel::sendMessage,
        onOpenChatDetailsPressed = viewModel::navigateToDetailsScreen,
        onLeaveChatPressed = viewModel::leaveChat
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
    onSendPressed: () -> Unit = {},
    onOpenChatDetailsPressed: () -> Unit = {},
    onLeaveChatPressed: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .navigationBarsPadding()
    ) {
        Column {
            MyTopAppBar(
                chatTitle = chatTitle,
                avatar = avatar,
                membersCount = membersCount,
                onBackPressed = onBackPressed,
                onOpenChatDetailsPressed = onOpenChatDetailsPressed,
                onLeaveChatPressed = onLeaveChatPressed
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
}

@Composable
fun MyTopAppBar(
    chatTitle: String, avatar: String?,
    membersCount: Int,
    onBackPressed: () -> Unit,
    onOpenChatDetailsPressed: () -> Unit,
    onLeaveChatPressed: () -> Unit
) {
    com.google.accompanist.insets.ui.TopAppBar(
        title = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(
                        interactionSource = MutableInteractionSource(),
                        indication = null,
                        onClick = onOpenChatDetailsPressed
                    )
            ) {
                AutoAvatar(
                    title = chatTitle,
                    url = avatar,
                    size = 40.dp,
                    modifier = Modifier.padding(end = 8.dp)
                )

                Column {
                    Text(chatTitle, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text(
                        "участников: $membersCount",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                        color = LocalContentColor.current.copy(alpha = ContentAlpha.medium)
                    )
                }
            }
        },
        contentPadding = WindowInsets.statusBars.asPaddingValues(),
        navigationIcon = {
            IconButton(onClick = onBackPressed) {
                Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Go back")
            }
        },
        actions = {
            ChatOptionsMenuAction(onLeaveChatPressed)
        }
    )
}

@Composable
fun ChatOptionsMenuAction(onLeaveChatPressed: () -> Unit) {
    var dropdownMenuExpanded by remember { mutableStateOf(false) }

    Box {
        IconButton(onClick = { dropdownMenuExpanded = true }) {
            Icon(Icons.Filled.MoreVert, stringResource(R.string.more_actions))
        }

        DropdownMenu(
            expanded = dropdownMenuExpanded,
            onDismissRequest = { dropdownMenuExpanded = false },
            modifier = Modifier.padding(0.dp)
        ) {
            DropdownMenuItem(onClick = {
                dropdownMenuExpanded = false
                onLeaveChatPressed()
            }) {
                Icon(Icons.Filled.Logout, contentDescription = stringResource(R.string.leave_chat))
                Spacer(Modifier.width(16.dp))
                Text(stringResource(R.string.leave_chat))
            }
        }
    }
}

@Composable
fun MessagesList(
    currentUserId: String,
    messages: List<Message>,
    modifier: Modifier = Modifier,
    state: LazyListState
) {
    val myMessageRowModifier = Modifier
        .padding(top = 8.dp, start = 70.dp)
        .fillMaxWidth()

    val otherMessageRowModifier = Modifier
        .padding(top = 8.dp, end = 70.dp)
        .fillMaxWidth()

    val avatarModifier = Modifier.padding(top = 12.dp, end = 8.dp)

    val messageModifier = Modifier
        .clip(RoundedCornerShape(18.dp))
        .background(MaterialTheme.colors.surface)
        .padding(horizontal = 12.dp, vertical = 8.dp)

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

            val arrangement = if (isMyMsg) Arrangement.End else Arrangement.Start
            val rowModifier = if (isMyMsg)
                myMessageRowModifier
            else
                otherMessageRowModifier

            Row(
                modifier = rowModifier,
                horizontalArrangement = arrangement
            ) {
                if (!isMyMsg) {
                    AutoAvatar(
                        title = message.from.name,
                        url = message.from.avatar,
                        size = 32.dp,
                        modifier = avatarModifier
                    )
                }

                Message(
                    author = author,
                    content = message.content,
                    sentTime = message.sentTime,
                    modifier = messageModifier
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
            messagesListState = rememberLazyListState(),
            onLeaveChatPressed = {}
        )
    }
}
