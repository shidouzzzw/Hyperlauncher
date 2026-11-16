package com.gafure.hyperlauncher.ui.components

import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** One row in a long-press menu. */
@Immutable
data class ContextAction(
    val label: String,
    val onClick: () -> Unit,
)

/** Small anchored menu used for both home icons and drawer icons. */
@Composable
fun AppContextMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    actions: List<ContextAction>,
    modifier: Modifier = Modifier,
) {
    if (actions.isEmpty()) return
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        modifier = modifier.widthIn(min = 200.dp),
    ) {
        actions.forEach { action ->
            DropdownMenuItem(
                text = { Text(text = action.label, style = MaterialTheme.typography.bodyMedium) },
                onClick = {
                    onDismiss()
                    action.onClick()
                },
            )
        }
    }
}
