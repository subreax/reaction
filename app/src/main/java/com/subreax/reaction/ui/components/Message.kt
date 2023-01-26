package com.subreax.reaction.ui.components

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.subreax.reaction.ui.theme.ReactionTheme
import java.text.SimpleDateFormat
import kotlin.math.max

@Composable
fun MessageLayout(
    message: @Composable () -> Unit,
    info: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    horizontalGap: Dp = 0.dp,
    verticalGap: Dp = 0.dp
) {
    Layout(modifier = modifier, content = { message(); info() }) { measurables, constraints ->
        check(measurables.size == 2)

        val placeables = measurables.map {
            it.measure(constraints)
        }
        val msg = placeables[0]
        val info = placeables[1]

        var infoX = 0
        var infoY = 0

        var width = msg.width + horizontalGap.roundToPx() + info.width
        var height = msg.height
        infoX = msg.width + horizontalGap.roundToPx()
        infoY = height - info.height

        if (width > constraints.maxWidth) {
            width = max(msg.width, info.width)
            height = msg.height + verticalGap.roundToPx() + info.height

            infoX = width - info.width
            infoY = msg.height + verticalGap.roundToPx()
        }

        layout(width, height) {
            msg.placeRelative(x = 0, y = 0)
            info.placeRelative(x = infoX, y = infoY)
        }
    }
}


@Composable
fun Message(
    author: String?,
    content: String,
    sentTime: Long,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier.clip(RoundedCornerShape(18.dp))
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            if (author != null) {
                Text(
                    text = author,
                    style = MaterialTheme.typography.body1.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    ),
                    color = MaterialTheme.colors.primary
                )
            }

            MessageLayout(
                message = { Text(content) },
                info = {
                    CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                        Text(
                            text = SimpleDateFormat.getTimeInstance(SimpleDateFormat.SHORT)
                                .format(sentTime),
                            fontSize = 10.sp,
                        )
                    }
                },
                horizontalGap = 6.dp
            )
        }
    }
}


private fun formatTime(time: Long): String {
    return SimpleDateFormat.getTimeInstance(SimpleDateFormat.SHORT).format(time)
}


@Preview
@Preview(uiMode = UI_MODE_NIGHT_YES)
@Composable
fun MessagePreview() {
    ReactionTheme {
        Message(
            author = "refrigerator2k",
            content = "Hello world",
            sentTime = 1674310839L
        )
    }
}

@Preview(uiMode = UI_MODE_NIGHT_YES, widthDp = 120)
@Composable
fun MessagePreview1() {
    ReactionTheme {
        Message(
            author = null,
            content = "Hello world",
            sentTime = 1674310839L
        )
    }
}