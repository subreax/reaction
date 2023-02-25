package com.subreax.reaction.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun OptionItem(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp),
    trailingSection: @Composable RowScope.() -> Unit = {}
) {
    Row(
        modifier = Modifier
            .clickable(onClick = onClick)
            .fillMaxWidth()
            .heightIn(48.dp)
            .padding(contentPadding),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, title, modifier = Modifier.padding(end = 16.dp))
        Text(title)
        Spacer(Modifier.weight(1.0f))
        trailingSection()
    }
}