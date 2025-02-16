package dev.silas.view

import androidx.compose.foundation.layout.Column
import androidx.compose.material.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import kotlinx.browser.window
import kotlinx.coroutines.await

@Composable
fun CustomTextField(
    modifier: Modifier = Modifier,
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    label: @Composable (() -> Unit)? = null,
    isError: (() -> Boolean)? = { false }
) {
    var shortcutEvent: ShortcutEvent? by remember { mutableStateOf(null) }
    var isCtrlPressed by remember { mutableStateOf(false) }
    var isMetaPressed by remember { mutableStateOf(false) }

    LaunchedEffect(shortcutEvent) {
        ShortcutEventHandler(
            shortcutEvent = shortcutEvent,
            value = value,
            onValueChange = onValueChange
        )
        shortcutEvent = null
    }
    Column(
        modifier = Modifier
            .onPreviewKeyEvent {
                isCtrlPressed = it.isCtrlPressed
                isMetaPressed = it.isMetaPressed
                shortcutEvent = it.filterKeyDown()?.toShortcutEvent()
                false
            }
    ) {
        OutlinedTextField(
            value = value,
            label = label,
            isError = when (isError != null) {
                true -> isError()
                else -> false
            },
            onValueChange = {
                if (!isCtrlPressed && !isMetaPressed) onValueChange(it)
            },
            modifier = modifier
        )
    }
}

enum class ShortcutEvent {
    CUT, COPY, PASTE, HIGHLIGHT_ALL
}

private suspend fun ShortcutEventHandler(
    shortcutEvent: ShortcutEvent?,
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit
) {
    shortcutEvent ?: return
    when (shortcutEvent) {
        ShortcutEvent.CUT -> {
            // The cut function seems to work same as copy, so we clear the selected value.
            onValueChange(value.replaceSelected(""))
        }

        ShortcutEvent.COPY -> {
            // Unused - seems to work out of the box
        }

        ShortcutEvent.PASTE -> {
            val clipboardText = window.navigator.clipboard.readText().await<String>()
            onValueChange(value.replaceSelected(clipboardText))
        }

        ShortcutEvent.HIGHLIGHT_ALL -> {
            onValueChange(value.copy(selection = TextRange(0, value.text.length)))
        }
    }
}

private fun KeyEvent.filterKeyDown() =
    if (type == KeyEventType.KeyDown) this else null

private fun KeyEvent.toShortcutEvent() = when {
    (isMetaPressed || isCtrlPressed) && key == Key.X -> ShortcutEvent.CUT
    (isMetaPressed || isCtrlPressed) && key == Key.V -> ShortcutEvent.PASTE
    (isMetaPressed || isCtrlPressed) && key == Key.A -> ShortcutEvent.HIGHLIGHT_ALL
    else -> null
}

private fun TextFieldValue.replaceSelected(replacement: String) =
    copy(text = text.replaceRange(selection.min, selection.max, replacement), TextRange(0, 0))
