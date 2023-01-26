package com.subreax.reaction.ui

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.subreax.reaction.ui.theme.ReactionTheme

@Composable
fun CustomTextField(
    value: String,
    modifier: Modifier = Modifier,
    onValueChange: (String) -> Unit,
    hint: String = "",
    singleLine: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(hint) },
        colors = TextFieldDefaults.outlinedTextFieldColors(
            backgroundColor = MaterialTheme.colors.onSurface.copy(alpha = 0.1f),
            unfocusedBorderColor = Color.Transparent
        ),
        singleLine = singleLine,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        modifier = modifier,
        visualTransformation = visualTransformation
    )
}

@Preview(showBackground = true)
@Composable
fun CustomTextFieldPreview() {
    ReactionTheme {
        Surface {
            CustomTextField(
                value = "",
                onValueChange = {},
                hint = "Hint",
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES)
@Composable
fun CustomTextFieldPreviewDark() {
    ReactionTheme {
        Surface {
            CustomTextField(
                value = "",
                onValueChange = {},
                hint = "Hint",
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}