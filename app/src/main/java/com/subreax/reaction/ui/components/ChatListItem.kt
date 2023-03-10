package com.subreax.reaction.ui.components

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.subreax.reaction.api.User
import com.subreax.reaction.data.chat.Message
import com.subreax.reaction.ui.theme.ReactionTheme
import java.text.SimpleDateFormat
import kotlin.math.floor


@Composable
fun ChatListItem(
    chatId: String,
    chatName: String,
    avatar: String?,
    lastMessage: Message,
    unreadMessagesCount: Int,
    isMuted: Boolean,
    isPinned: Boolean,
    modifier: Modifier = Modifier
) {
    val surfaceColor = MaterialTheme.colors.surface
    val backgroundColor: Color
    if (isPinned) {
        backgroundColor = LocalElevationOverlay.current?.apply(
            color = surfaceColor,
            elevation = 2.dp
        ) ?: surfaceColor
    }
    else {
        backgroundColor = surfaceColor
    }


    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .background(backgroundColor)
            .padding(8.dp)
    ) {
        AutoAvatar(colorStr = chatId, title = chatName, url = avatar, size = 56.dp)

        ChatListItemBody(
            chatName = chatName,
            msgSender = lastMessage.from.name,
            msg = lastMessage.content,
            isMuted = isMuted,
            modifier = Modifier.weight(1.0f)
        )

        Box(modifier = Modifier.height(48.dp)) {
            Text(
                text = formatTime(lastMessage.sentTime),
                fontSize = 13.sp,
                color = LocalContentColor.current.copy(alpha = ContentAlpha.disabled),
                modifier = Modifier.align(Alignment.TopEnd)
            )

            Row(
                modifier = Modifier.align(Alignment.BottomEnd),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (unreadMessagesCount > 0) {
                    MessagesCounter(
                        count = unreadMessagesCount,
                        color =
                        if (!isMuted)
                            MaterialTheme.colors.primary
                        else
                            MaterialTheme.colors.onSurface.copy(alpha = 0.2f)
                    )
                } else if (isPinned) {
                    OutlinedChatCircleIcon {
                        Icon(
                            imageVector = Icons.Default.PushPin,
                            contentDescription = "Chat is pinned",
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatListItemBody(
    chatName: String,
    msgSender: String,
    msg: String,
    isMuted: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(horizontal = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = chatName,
                style = MaterialTheme.typography.body1.copy(fontWeight = FontWeight.Bold),
                overflow = TextOverflow.Ellipsis,
                maxLines = 1
            )

            if (isMuted) {
                Icon(
                    imageVector = Icons.Default.VolumeOff,
                    contentDescription = "Chat volume is off",
                    modifier = Modifier
                        .padding(start = 4.dp)
                        .size(16.dp),
                    tint = LocalContentColor.current.copy(alpha = ContentAlpha.disabled)
                )
            }
        }

        Row {
            Text(
                text = msgSender,
                style = MaterialTheme.typography.body1,
                color = LocalContentColor.current.copy(alpha = ContentAlpha.medium),
                modifier = Modifier.padding(end = 4.dp),
                overflow = TextOverflow.Ellipsis,
                maxLines = 1
            )

            Text(
                text = msg,
                style = MaterialTheme.typography.body1,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1
            )
        }
    }
}

private fun formatTime(time: Long): String {
    val timeDiffMs = System.currentTimeMillis() - time
    if (timeDiffMs < 1000 * 60 * 60 * 24) {
        return SimpleDateFormat.getTimeInstance(SimpleDateFormat.SHORT).format(time)
    }

    return SimpleDateFormat.getDateInstance(SimpleDateFormat.MEDIUM).format(time)
}

private fun countToString(count: Int): String {
    if (count < 999) {
        return "$count"
    }

    if (count < 99999) {
        val countf = floor(count / 100.0f) / 10.0f
        return "${countf}k"
    }

    return "????????????"
}

@Composable
fun ChatCircleThing(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colors.onSurface.copy(alpha = 0.2f),
    contentColor: Color = contentColorFor(color),
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(color)
            .sizeIn(minWidth = 24.dp, minHeight = 24.dp)
            .padding(horizontal = 5.dp),
        contentAlignment = Alignment.Center
    ) {
        CompositionLocalProvider(LocalContentColor provides contentColor) {
            ProvideTextStyle(
                MaterialTheme.typography.body1.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            ) {
                content()
            }
        }
    }
}

@Composable
fun OutlinedChatCircleIcon(
    modifier: Modifier = Modifier,
    icon: @Composable () -> Unit
) {
    val shape = RoundedCornerShape(50)

    CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.disabled) {
        val borderColor = LocalContentColor.current.copy(alpha = LocalContentAlpha.current)
        Box(
            modifier = modifier
                .clip(shape)
                .border(width = 1.dp, color = borderColor, shape = shape)
                .sizeIn(minWidth = 24.dp, minHeight = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            icon()
        }
    }
}

@Composable
fun MessagesCounter(
    count: Int,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colors.primary,
    contentColor: Color = contentColorFor(color)
) {
    ChatCircleThing(modifier, color = color, contentColor = contentColor) {
        Text(countToString(count))
    }
}

@Preview
@Composable
fun MessagesCounterPreview() {
    ReactionTheme {
        MessagesCounter(count = 102)
    }
}

@Preview(widthDp = 360)
@Preview(widthDp = 360, uiMode = UI_MODE_NIGHT_YES)
@Composable
fun ChatListItemPreview() {
    ReactionTheme {
        Surface(color = MaterialTheme.colors.background) {
            ChatListItem(
                chatId = "",
                chatName = "ChatName",
                avatar = null,
                lastMessage = Message(
                    "",
                    User(
                        "",
                        "refrigerator2k",
                        null,
                        System.currentTimeMillis()
                    ),
                    "message",
                    1673546695L
                ),
                unreadMessagesCount = 0,
                isPinned = true,
                isMuted = true
            )
        }
    }
}
