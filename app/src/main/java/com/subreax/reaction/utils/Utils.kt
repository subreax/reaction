package com.subreax.reaction.utils

import android.content.res.Resources
import android.os.Build
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.text.TextUtils
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

fun <K, V> MutableMap<K, V>.putSynchronously(key: K, value: V) {
    synchronized(this) {
        put(key, value)
    }
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

fun Int.toDp(): Dp {
    return (this / Resources.getSystem().displayMetrics.density).roundToInt().dp
}
