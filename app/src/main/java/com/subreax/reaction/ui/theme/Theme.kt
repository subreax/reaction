package com.subreax.reaction.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorPalette = darkColors(
    primary = Green,
    primaryVariant = DarkGreen,
    onPrimary = Color(0xffE4E4E4),
    secondary = Green,
    surface = Gray900,
    onSurface = Color(0xffE4E4E4),/*
    onBackground = Color(0xff424242)*/
)

private val LightColorPalette = lightColors(
    primary = Green,
    primaryVariant = DarkGreen,
    secondary = Green

    /* Other default colors to override
    background = Color.White,
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = Color.Black,
    onSurface = Color.Black,
    */
)

@Composable
fun ReactionTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(
        colors = if (darkTheme) DarkColorPalette else LightColorPalette,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}