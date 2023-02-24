package com.subreax.reaction.ui.chat

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.subreax.reaction.R
import com.subreax.reaction.api.User
import com.subreax.reaction.data.ApplicationState
import com.subreax.reaction.data.chat.Message
import com.subreax.reaction.ui.components.AutoAvatar
import com.subreax.reaction.ui.components.LoadingOverlay
import com.subreax.reaction.ui.components.Message
import com.subreax.reaction.ui.theme.ReactionTheme
import com.subreax.reaction.utils.pluralResource

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

    LaunchedEffect(uiState.messages) {
        // scroll only if the user at the bottom of the list
        // or the user sent a message
        if (messagesListState.firstVisibleItemIndex < 2
            || uiState.messages.firstOrNull()?.from?.id == viewModel.userId
        ) {
            messagesListState.animateScrollToItem(0)
        }
    }

    ChatScreen(
        isLoading = uiState.isLoading,
        appState = uiState.appState,
        currentUserId = viewModel.userId,
        chatId = uiState.chatId,
        chatTitle = uiState.title,
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
    appState: ApplicationState,
    currentUserId: String,
    chatId: String,
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
                appState = appState,
                chatId = chatId,
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
    appState: ApplicationState,
    chatId: String,
    chatTitle: String,
    avatar: String?,
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
                    colorStr = chatId,
                    title = chatTitle,
                    url = avatar,
                    size = 40.dp,
                    modifier = Modifier.padding(end = 8.dp)
                )

                Column {
                    Text(chatTitle, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    MembersCounterText(appState, membersCount)
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
fun MembersCounterText(appState: ApplicationState, membersCount: Int) {
    val text = when (appState) {
        ApplicationState.WaitingForNetwork -> stringResource(R.string.waiting_for_network)
        ApplicationState.Connecting -> stringResource(R.string.connecting)
        ApplicationState.Syncing -> stringResource(R.string.syncing)
        ApplicationState.Ready -> pluralResource(R.plurals.members_count, membersCount, membersCount)
    }

    Text(
        text,
        fontSize = 14.sp,
        fontWeight = FontWeight.Normal,
        color = LocalContentColor.current.copy(alpha = ContentAlpha.medium)
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
        reverseLayout = true,
        state = state
    ) {
        items(messages, key = { it.sentTime }) { message ->
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
                        colorStr = message.from.id,
                        title = message.from.name,
                        url = message.from.avatar,
                        size = 32.dp,
                        modifier = avatarModifier
                    )
                }

                Message(
                    userId = message.from.id,
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
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { /*TODO*/ }) {
                Icon(imageVector = Icons.Filled.Mood, contentDescription = "Emoji icon")
            }
            MessageInputTextField(
                value = message,
                onValueChange = onMessageChanged,
                hint = stringResource(R.string.message),
                modifier = Modifier.weight(1.0f).padding(vertical = 8.dp),
                maxLines = 6
            )
            IconButton(onClick = onSendPressed) {
                Icon(imageVector = Icons.Filled.Send, contentDescription = "Send")
            }
        }
    }
}

@Composable
fun MessageInputTextField(
    value: String,
    onValueChange: (String) -> Unit,
    hint: String,
    modifier: Modifier = Modifier,
    textStyle: TextStyle = MaterialTheme.typography.body1,
    maxLines: Int = Int.MAX_VALUE
) {
    val color = LocalContentColor.current

    Box(modifier = modifier) {
        if (value.isEmpty()) {
            Text(
                text = hint,
                color = color.copy(alpha = ContentAlpha.medium),
                style = textStyle
            )
        }

        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = textStyle.copy(color = color),
            cursorBrush = SolidColor(MaterialTheme.colors.primary),
            modifier = Modifier.fillMaxWidth(),
            maxLines = maxLines
        )
    }
}

@Preview(uiMode = UI_MODE_NIGHT_YES, widthDp = 400, heightDp = 700, showBackground = true)
@Composable
fun ChatScreenPreview() {
    val user = User("123", "refrigerator2k", null, System.currentTimeMillis())
    val other = User("567", "-Mipe-", null, System.currentTimeMillis())
    val messages = listOf(
        Message("", other, "нормальна", 0),
        Message("", user, "как дела", 1),
        Message("", user, "привет", 2)
    )

    ReactionTheme {
        ChatScreen(
            isLoading = false,
            appState = ApplicationState.Ready,
            currentUserId = "123",
            chatId = "",
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
