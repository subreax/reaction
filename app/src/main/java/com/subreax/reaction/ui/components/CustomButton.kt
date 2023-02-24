package com.subreax.reaction.ui.components

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.subreax.reaction.ui.theme.ReactionTheme

@Composable
fun CustomButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .widthIn(160.dp)
            .heightIn(48.dp),
        enabled = enabled
    ) {
        Text(text = text, color = MaterialTheme.colors.onPrimary)
    }
}

@Composable
fun CustomOutlinedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .widthIn(160.dp)
            .heightIn(48.dp),
        border = ButtonDefaults.outlinedBorder.copy(width = 2.dp),
        enabled = enabled
    ) {
        Text(text = text, color = MaterialTheme.colors.onSurface)
    }
}

@Preview
@Preview(uiMode = UI_MODE_NIGHT_YES, name = "Button Night")
@Composable
fun CustomButtonPreview() {
    ReactionTheme {
        Column {
            CustomButton(text = "Button", onClick = { })
            Spacer(Modifier.heightIn(32.dp))
            CustomOutlinedButton(text = "Button", onClick = { })
        }
    }
}
