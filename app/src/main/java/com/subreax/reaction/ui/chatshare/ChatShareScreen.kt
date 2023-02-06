package com.subreax.reaction.ui.chatshare

import android.graphics.Typeface
import android.text.Layout
import android.text.TextPaint
import android.text.TextUtils
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.layout
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.core.graphics.withSave
import com.google.zxing.common.BitMatrix
import com.subreax.reaction.R
import com.subreax.reaction.StaticLayout_createInstance
import com.subreax.reaction.colorGradientFor
import com.subreax.reaction.ui.components.AutoAvatar
import java.lang.Float.min
import kotlin.math.floor
import kotlin.math.round

@Composable
fun ChatShareScreen(
    viewModel: ChatShareViewModel,
    //colors: List<Color>,
    avatarSize: Dp = 72.dp,
    horizontalPadding: Dp = 48.dp,
    labelMaxHeight: Dp = 48.dp,
    onBackPressed: () -> Unit = {}
) {
    val uiState = viewModel.uiState

    if (uiState != null) {
        ChatShareScreen(
            qr = uiState.qr,
            text = uiState.chatName,
            colors = colorGradientFor(uiState.chatName),
            avatarSize = avatarSize,
            horizontalPadding = horizontalPadding,
            labelMaxHeight = labelMaxHeight,
            onBackPressed = onBackPressed
        )
    }
}

@Composable
fun ChatShareScreen(
    qr: BitMatrix,
    text: String,
    colors: List<Color>,
    avatarSize: Dp,
    horizontalPadding: Dp,
    labelMaxHeight: Dp,
    onBackPressed: () -> Unit
) {
    Surface(color = MaterialTheme.colors.background) {
        Column(Modifier.statusBarsPadding().navigationBarsPadding()) {
            IconButton(onClick = onBackPressed, modifier = Modifier.padding(4.dp)) {
                Icon(Icons.Filled.ArrowBack, stringResource(R.string.nav_back))
            }

            Box(Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier.align(Alignment.Center).offset(y = -avatarSize),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AutoAvatar(
                        title = text,
                        url = null,
                        size = avatarSize,
                        modifier = Modifier
                            .offset(y = avatarSize / 2)
                            .zIndex(2.0f)
                    )
                    Spacer(
                        modifier = Modifier
                            .padding(horizontal = horizontalPadding)
                            .clip(RoundedCornerShape(32.dp))
                            .background(Color(0xffefefef))
                            .layoutQr(
                                qr,
                                topPadding = 24.dp,
                                bottomPadding = 16.dp,
                                additionalVSpace = labelMaxHeight
                            )
                            .drawBehind {
                                val bmp = ImageBitmap(size.width.toInt(), size.height.toInt())
                                val canvas0 = Canvas(bmp)
                                val qrSize = min(size.width, size.height)

                                drawQrMask(canvas0, qr, qrSize.toInt())
                                drawTextMask(
                                    canvas0,
                                    text,
                                    qrSize,
                                    size.width * 0.8f,
                                    size.height - qrSize
                                )
                                drawGradient(canvas0, colors)

                                drawImage(bmp)
                            }
                    )
                }
            }
        }
    }
}


private fun Modifier.layoutQr(
    qr: BitMatrix,
    additionalVSpace: Dp = 0.dp,
    topPadding: Dp = 0.dp,
    bottomPadding: Dp = 0.dp
) = layout { measurable, constraints ->
    val minSide =
        if (constraints.maxWidth < constraints.maxHeight)
            constraints.maxWidth
        else
            constraints.maxHeight

    val qrMeasurement = measureQr(qr, minSide.toFloat())
    val width = qrMeasurement.size.toInt()
    var height = width + additionalVSpace.roundToPx()
    if (height > constraints.maxHeight) {
        height = constraints.maxHeight
    }

    val placeable = measurable.measure(
        Constraints(width, width, height, height)
    )
    layout(placeable.width, placeable.height + (topPadding + bottomPadding).roundToPx()) {
        placeable.placeRelative(0, topPadding.roundToPx())
    }
}


private fun drawTextMask(canvas: Canvas, text: String, y: Float, width: Float, height: Float) {
    val textSize = round(height * 0.65f)

    val textPaint = TextPaint().apply {
        color = android.graphics.Color.BLACK
        style = android.graphics.Paint.Style.FILL
        this.textSize = textSize
        typeface = Typeface.DEFAULT_BOLD
        isAntiAlias = true
    }

    val staticLayout = StaticLayout_createInstance(
        text = text,
        width = width.toInt(),
        textPaint = textPaint,
        maxLines = 1,
        alignment =  Layout.Alignment.ALIGN_CENTER,
        ellipsize = TextUtils.TruncateAt.END
    )

    canvas.nativeCanvas.withSave {
        val cx = (this.width - width) / 2
        translate(cx, y)
        staticLayout.draw(this)
    }
}


private data class QrMeasurement(
    val size: Float,
    val blockSz: Int
)

private fun measureQr(qr: BitMatrix, preferredSize: Float): QrMeasurement {
    var blockSz = floor(preferredSize / qr.width).toInt()
    if (blockSz % 2 != 0) {
        blockSz -= 1
    }
    return QrMeasurement(blockSz.toFloat() * qr.width, blockSz)
}

private fun drawQrMask(canvas: Canvas, qr: BitMatrix, size: Int) {
    val blockSz = (size / qr.width).toFloat()
    val blockSzInt = blockSz.toInt()
    val blockSz2 = blockSz / 2

    val whitePaint = Paint().also {
        it.color = Color(0xff000000)
        it.style = PaintingStyle.Fill
    }

    val bmpRoundMask = ImageBitmap(blockSzInt, blockSzInt, config = ImageBitmapConfig.Alpha8)
    with(Canvas(bmpRoundMask)) {
        drawCircle(Offset(blockSz2, blockSz2), blockSz2, whitePaint)
    }

    for (i in 0 until qr.width) {
        for (j in 0 until qr.height) {
            if (qr[i, j]) {
                val x = i * blockSzInt
                val xf = x.toFloat()
                val y = j * blockSzInt
                val yf = y.toFloat()
                val offset = Offset(xf, yf)

                canvas.drawImage(bmpRoundMask, offset, whitePaint)
                // draw top rect
                if (qr[i, j - 1]) {
                    canvas.drawRect(xf, yf, xf + blockSz, yf + blockSz2, whitePaint)
                }
                // draw bottom rect
                if (qr[i, j + 1]) {
                    canvas.drawRect(xf, yf + blockSz2, xf + blockSz, yf + blockSz, whitePaint)
                }
                // draw left rect
                if (qr[i - 1, j]) {
                    canvas.drawRect(xf, yf, xf + blockSz2, yf + blockSz, whitePaint)
                }
                // draw right rect
                if (qr[i + 1, j]) {
                    canvas.drawRect(xf + blockSz2, yf, xf + blockSz, yf + blockSz, whitePaint)
                }
            }
        }
    }
}


private fun drawGradient(canvas: Canvas, colors: List<Color>) {
    val width = canvas.nativeCanvas.width.toFloat()
    val height = canvas.nativeCanvas.height.toFloat()

    val paint = Paint().apply {
        style = PaintingStyle.Fill
        blendMode = BlendMode.SrcIn
    }

    val brush = Brush.linearGradient(
        colors = colors,
        start = Offset(0.0f, 0.0f),
        end = Offset(width, width)
    )
    brush.applyTo(Size(width, height), paint, 1.0f)
    canvas.drawRect(0.0f, 0.0f, width, height, paint)
}
