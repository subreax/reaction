package com.subreax.reaction

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