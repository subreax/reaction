package com.subreax.reaction.utils

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

sealed class UiText {
    class Res(@StringRes val id: Int, val args: Array<Any> = emptyArray()) : UiText()
    class Hardcoded(val str: String) : UiText()

    @Composable
    fun asString(): String {
        return when (this) {
            is Res -> stringResource(id, args)
            is Hardcoded -> str
        }
    }

    override fun toString(): String {
        return when (this) {
            is Res -> "res#$id"
            is Hardcoded -> str
        }
    }

    companion object {
        fun Empty() = Hardcoded("")
    }
}
