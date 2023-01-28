package com.subreax.reaction

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
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
