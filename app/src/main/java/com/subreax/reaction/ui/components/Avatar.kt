package com.subreax.reaction.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.subreax.reaction.ui.theme.ReactionTheme

@Composable
fun Avatar(url: String, size: Dp, modifier: Modifier = Modifier) {
    AsyncImage(
        model = url,
        contentDescription = "Avatar",
        modifier = modifier
            .size(size)
            .clip(CircleShape)
    )
}

@Composable
fun AvatarPlaceholder(title: String, size: Dp, modifier: Modifier = Modifier) {
    val sum = title.sumOf { it.code } * 55 + 15
    val hue = (sum.mod(360)).toFloat()
    val hueOffset = (hue + 45).mod(360.0f)
    val colorStart = Color.hsv(hue, 0.6f, 0.9f)
    val colorEnd = Color.hsv(hueOffset, 0.6f, 0.7f)

    val brush = Brush.verticalGradient(
        listOf(colorStart, colorEnd)
    )

    val fontSize = with(LocalDensity.current) {
        (size * 0.333f).toSp()
    }

    var wasSpace = true
    var charsCount = 0
    val text = title.filter {
        if (!wasSpace) {
            wasSpace = it == ' ' && charsCount < 2
            false
        }
        else {
            wasSpace = false
            charsCount += 1
            true
        }
    }.uppercase()

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(brush)
    ) {
        Text(
            text = text,
            modifier = Modifier.align(Alignment.Center),
            color = Color.White,
            fontSize = fontSize
        )
    }
}

@Composable
fun AutoAvatar(title: String, url: String?, size: Dp, modifier: Modifier = Modifier) {
    if (url != null) {
        Avatar(url = url, size = size, modifier)
    } else {
        AvatarPlaceholder(title = title, size = size, modifier)
    }
}


@Preview
@Composable
fun AvatarPlaceholderPreview() {
    ReactionTheme {
        AvatarPlaceholder(title = "refrigerator2k", size = 48.dp)
    }
}