package com.subreax.reaction

import android.os.Build
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.text.TextUtils
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource

// Returns strRes if provided
class StringResource(
    private val str: String = "",
    @StringRes private val strRes: Int? = null
) {
    val value: String
        @Composable
        get() =
            if (strRes != null)
                stringResource(id = strRes)
            else
                str
}

fun String.toStringResource(): StringResource = StringResource(str = this)
fun Int.toStringResource(): StringResource = StringResource(strRes = this)

fun <K, V> MutableMap<K, V>.putSynchronously(key: K, value: V) {
    synchronized(this) {
        put(key, value)
    }
}

fun colorGradientFor(str: String): List<Color> {
    val sum = str.sumOf { it.code } * 55 + 15
    val hue = (sum.mod(360)).toFloat()
    val hueOffset = (hue + 30).mod(360.0f)
    val colorStart = Color.hsv(hue, 0.6f, 0.9f)
    val colorEnd = Color.hsv(hueOffset, 0.6f, 0.7f)
    return listOf(colorStart, colorEnd)
}

fun StaticLayout_createInstance(
    text: CharSequence,
    width: Int,
    textPaint: TextPaint,
    maxLines: Int = Int.MAX_VALUE,
    alignment: Layout.Alignment = Layout.Alignment.ALIGN_NORMAL,
    ellipsize: TextUtils.TruncateAt? = null,
    spacingMult: Float = 1.0f,
    spacingAdd: Float = 0.0f,
    start: Int = 0,
    end: Int = text.length
): StaticLayout {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        StaticLayout.Builder
            .obtain(text, start, end, textPaint, width)
            .setAlignment(alignment)
            .setEllipsize(ellipsize)
            .setMaxLines(maxLines)
            .setLineSpacing(spacingAdd, spacingMult)
            .build()
    }
    else {
        StaticLayout(
            text,
            start, end,
            textPaint,
            width,
            alignment,
            spacingMult, spacingAdd,
            false,
            ellipsize,
            width
        )
    }
}