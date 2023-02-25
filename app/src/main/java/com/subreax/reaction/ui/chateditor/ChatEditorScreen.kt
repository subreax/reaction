package com.subreax.reaction.ui.chateditor

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Done
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.accompanist.insets.ui.TopAppBar
import com.subreax.reaction.R
import com.subreax.reaction.ui.LocalStatusBarPadding
import com.subreax.reaction.ui.components.AutoAvatar
import com.subreax.reaction.ui.components.OptionItem
import com.subreax.reaction.ui.components.SimpleTextField
import com.subreax.reaction.ui.theme.ReactionTheme

@Composable
fun ChatEditorScreen(viewModel: ChatEditorViewModel) {
    ChatEditorScreen(
        chatId = viewModel.chatId,
        name = viewModel.chatName,
        avatar = viewModel.chatAvatar,
        showWarnDialog = viewModel.showWarningDialog,
        onNameChanged = viewModel::updateChatName,
        onCommitChanges = viewModel::applyChanges,
        onDiscardChanges = viewModel::discardChanges,
        onBackPressed = viewModel::navBack,
        onDismissDialog = viewModel::dismissWarningDialog
    )
}

@Composable
fun ChatEditorScreen(
    chatId: String,
    name: String,
    avatar: String?,
    showWarnDialog: Boolean,
    onNameChanged: (String) -> Unit,
    onCommitChanges: () -> Unit,
    onDiscardChanges: () -> Unit,
    onDismissDialog: () -> Unit,
    onBackPressed: () -> Unit
) {
    val nameTextFieldFR = FocusRequester()
    LaunchedEffect(true) {
        nameTextFieldFR.requestFocus()
    }

    if (showWarnDialog) {
        UnsavedChangesDialog(
            onCommit = onCommitChanges,
            onDiscard = onDiscardChanges,
            onDismiss = onDismissDialog
        )
    }

    Column {
        TopAppBar(
            title = {
                Text(text = stringResource(R.string.chat_editor))
            },
            contentPadding = LocalStatusBarPadding.current,
            navigationIcon = {
                IconButton(onClick = onBackPressed) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = stringResource(R.string.nav_back)
                    )
                }
            },
            actions = {
                IconButton(onClick = onCommitChanges) {
                    Icon(
                        imageVector = Icons.Default.Done,
                        contentDescription = stringResource(R.string.commit_changes)
                    )
                }
            }
        )

        ChatEditorScreenBody(
            chatId = chatId,
            name = name,
            avatar = avatar,
            onNameChanged = onNameChanged,
            nameTextFieldFocusRequester = nameTextFieldFR
        )
    }
}

@Composable
private fun ChatEditorScreenBody(
    chatId: String,
    name: String,
    avatar: String?,
    onNameChanged: (String) -> Unit,
    nameTextFieldFocusRequester: FocusRequester = FocusRequester()
) {
    Surface(Modifier.fillMaxWidth()) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
            ) {
                AutoAvatar(colorStr = chatId, title = name, url = avatar, size = 64.dp)
                SimpleTextField(
                    value = name,
                    onValueChanged = onNameChanged,
                    hint = stringResource(R.string.name_for_a_chat),
                    modifier = Modifier
                        .padding(start = 16.dp)
                        .weight(1.0f)
                        .focusRequester(nameTextFieldFocusRequester)
                )
            }
            OptionItem(
                icon = Icons.Default.AddAPhoto,
                title = stringResource(R.string.change_avatar),
                onClick = { /*TODO*/ })
        }
    }
}

@Composable
fun UnsavedChangesDialog(
    onCommit: () -> Unit,
    onDiscard: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onCommit) {
                Text(text = stringResource(R.string.yes))
            }
        },
        dismissButton = {
            TextButton(onClick = onDiscard) {
                Text(stringResource(R.string.no))
            }
        },
        title = { Text(stringResource(R.string.warning)) },
        text = {
            Text(stringResource(R.string.warning_unsaved_changes))
        },
        shape = RoundedCornerShape(16.dp)
    )
}

@Preview(uiMode = UI_MODE_NIGHT_YES, heightDp = 720, showBackground = true)
@Composable
fun ChatEditorPreview() {
    ReactionTheme {
        ChatEditorScreen(
            chatId = "",
            name = "",
            avatar = null,
            showWarnDialog = true,
            onNameChanged = {},
            onCommitChanges = {},
            onDiscardChanges = {},
            onDismissDialog = {},
            onBackPressed = {}
        )
    }
}