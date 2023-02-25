package com.subreax.reaction.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp


@Composable
fun SimpleTextField(
    value: String,
    onValueChanged: (String) -> Unit,
    modifier: Modifier = Modifier,
    hint: String = ""
) {
    var isFocused by remember { mutableStateOf(false) }

    val focusLineColor by animateColorAsState(
        if (isFocused)
            MaterialTheme.colors.primary
        else
            LocalContentColor.current.copy(alpha = ContentAlpha.disabled)
    )

    val focusLineThickness = if (isFocused) 2.dp else 1.dp

    BasicTextField(
        value = value,
        onValueChange = onValueChanged,
        textStyle = MaterialTheme.typography.body1.copy(
            color = LocalContentColor.current
        ),
        cursorBrush = SolidColor(LocalContentColor.current),
        modifier = modifier
            .fillMaxWidth()
            .onFocusChanged { isFocused = it.isFocused },
        decorationBox = { innerTextField ->
            Box(Modifier.heightIn(36.dp)) {
                if (value.isEmpty()) {
                    Text(
                        text = hint,
                        style = MaterialTheme.typography.body1,
                        color = LocalContentColor.current.copy(alpha = ContentAlpha.disabled),
                        modifier = Modifier.align(Alignment.CenterStart)
                    )
                }

                Box(Modifier.align(Alignment.CenterStart).fillMaxWidth()) {
                    innerTextField()
                }

                Divider(
                    thickness = focusLineThickness,
                    color = focusLineColor,
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                )
            }
        }
    )
}