package com.subreax.reaction.utils

import androidx.annotation.PluralsRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

@Composable
fun pluralResource(@PluralsRes pluralRes: Int, quantity: Int): String {
    return LocalContext.current.resources.getQuantityString(pluralRes, quantity)
}

@Composable
fun pluralResource(@PluralsRes pluralRes: Int, quantity: Int, vararg args: Any): String {
    return LocalContext.current.resources.getQuantityString(pluralRes, quantity, *args)
}
