package com.subreax.reaction.ui.components

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.graphics.Paint
import android.text.StaticLayout
import android.text.TextPaint
import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.subreax.reaction.utils.StaticLayout_createInstance
import com.subreax.reaction.ui.theme.ReactionTheme
import java.text.SimpleDateFormat
import kotlin.math.max
import kotlin.math.roundToInt

@Composable
fun MessageTextAndInfo(
    text: String,
    info: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    textColor: Color = MaterialTheme.colors.onSurface
) {
    val textSz = with(LocalDensity.current) {
        MaterialTheme.typography.body1.fontSize.toPx()
    }

    val textPaint = TextPaint().apply {
        textSize = textSz
        style = Paint.Style.FILL
        color = textColor.toArgb()
        isAntiAlias = true
    }

    var staticLayout: StaticLayout? = null

    Layout(
        modifier = modifier
            .drawWithCache {
                onDrawBehind {
                    drawIntoCanvas {
                        staticLayout?.draw(it.nativeCanvas)
                            ?: Log.e(
                                "Message",
                                "Failed to draw message because staticLayout = null"
                            )
                    }
                }
            },
        content = info
    ) { measurables, constraints ->
        val maxWidth = constraints.maxWidth
        val placeables = measurables.map { it.measure(constraints) }
        val info = placeables[0]

        val sl = StaticLayout_createInstance(text, constraints.maxWidth, textPaint)
        staticLayout = sl

        val textWidth = sl.measureTextWidth(text, textPaint)
        val lastLinePlusInfoWidth = sl.measureLastLineWidth(text, textPaint) + info.width

        var width = textWidth
        var height = sl.height
        var infoX = 0
        var infoY = 0

        if (lastLinePlusInfoWidth < maxWidth) {
            width = max(width, lastLinePlusInfoWidth)
            infoX = width - info.width
            infoY = height - info.height
        } else {
            infoX = width - info.width
            infoY = height
            height += info.height
        }

        layout(width, height) {
            info.placeRelative(infoX, infoY)
        }
    }
}

private fun StaticLayout.measureTextWidth(text: CharSequence, textPaint: TextPaint): Int {
    var width = 0.0f
    var lineStartIdx = 0
    for (line in 0 until lineCount) {
        val lineEndIdx = getLineVisibleEnd(line)
        val lineWidth = textPaint.measureText(text, lineStartIdx, lineEndIdx)
        if (width < lineWidth) {
            width = lineWidth
        }
        lineStartIdx = lineEndIdx
    }
    return width.roundToInt()
}

private fun StaticLayout.measureLastLineWidth(text: CharSequence, textPaint: TextPaint): Int {
    return textPaint.measureText(text, getLineStart(lineCount - 1), text.length).roundToInt()
}


private val messageInfoModifier = Modifier.padding(start = 8.dp)

@Composable
fun Message(
    author: String?,
    content: String,
    sentTime: Long,
    modifier: Modifier = Modifier
) {
    Column(modifier) {
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

        MessageTextAndInfo(
            text = content,
            info = {
                Text(
                    text = formatTime(sentTime),
                    modifier = messageInfoModifier,
                    style = MaterialTheme.typography.body1.copy(
                        fontSize = 10.sp,
                        color = MaterialTheme.colors.onSurface.copy(alpha = ContentAlpha.medium)
                    )
                )
            }
        )
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