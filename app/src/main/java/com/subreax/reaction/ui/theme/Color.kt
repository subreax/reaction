package com.subreax.reaction.ui.theme

import androidx.compose.ui.graphics.Color

val Purple200 = Color(0xFFBB86FC)
val Purple500 = Color(0xFF6200EE)
val Purple700 = Color(0xFF3700B3)
val Teal200 = Color(0xFF03DAC5)


val Green = Color(0xff39A437)
val DarkGreen = Color(0xff297527)
val Gray900 = Color(0xff212121)
val Gray800 = Color(0xff424242)


val pantone184 = Color(0xFFDF6B7C)
val pantone185 = Color(0xFFD93740)

val pantone163 = Color(0xFFEBA677)
val pantone164 = Color(0xFFE68A48)

val pantone346 = Color(0xFF87C597)
val pantone347 = Color(0xFF00A351)

val pantone305 = Color(0xFF73C6DC)
val pantone306 = Color(0xFF00B3DB)

val pantone245 = Color(0xFFC987B5)
val pantone246 = Color(0xFFB04590)

val colors = arrayOf(
    pantone184,
    pantone163,
    pantone346,
    pantone305,
    pantone245
)

val colorGradients = arrayOf(
    listOf(pantone184, pantone185),
    listOf(pantone163, pantone164),
    listOf(pantone346, pantone347),
    listOf(pantone305, pantone306),
    listOf(pantone245, pantone246)
)

fun String.toColor(): Color {
    val sum = this.sumOf { it.code }
    return colors[sum % colors.size]
}

fun String.toGradient(): List<Color> {
    val sum = this.sumOf { it.code }
    return colorGradients[sum % colors.size]
}
