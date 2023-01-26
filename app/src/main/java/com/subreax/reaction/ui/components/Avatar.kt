package com.subreax.reaction.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import coil.compose.AsyncImage

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
fun AvatarPlaceholder(size: Dp, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(MaterialTheme.colors.primary)
    )
}

@Composable
fun AutoAvatar(url: String?, size: Dp, modifier: Modifier = Modifier) {
    if (url != null) {
        Avatar(url = url, size = size, modifier)
    }
    else {
        AvatarPlaceholder(size = size, modifier)
    }
}