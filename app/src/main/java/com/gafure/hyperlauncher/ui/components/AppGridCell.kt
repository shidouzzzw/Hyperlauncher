package com.gafure.hyperlauncher.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gafure.hyperlauncher.model.AppInfo
import kotlinx.collections.immutable.persistentListOf

private val LabelShadow = Shadow(
    color = Color.Black.copy(alpha = 0.45f),
    offset = Offset(0f, 1f),
    blurRadius = 6f,
)

/** Hoisted so the label never re-derives a TextStyle during composition. */
private val LabelStyle = TextStyle(
    fontFamily = FontFamily.SansSerif,
    fontWeight = FontWeight.Normal,
    fontSize = 12.sp,
    lineHeight = 16.sp,
    letterSpacing = 0.2.sp,
    color = Color.White,
    shadow = LabelShadow,
)

/** One home-grid cell: squircle icon, single-line label and the long-press menu. */
@Composable
fun AppGridCell(
    app: AppInfo,
    onOpen: () -> Unit,
    onRemoveFromHome: () -> Unit,
    onAppInfo: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var menuOpen by remember(app.key) { mutableStateOf(false) }
    val currentOnRemove by rememberUpdatedState(onRemoveFromHome)
    val currentOnAppInfo by rememberUpdatedState(onAppInfo)
    val actions = remember(app.key) {
        persistentListOf(
            ContextAction(label = "Kaldır") { currentOnRemove() },
            ContextAction(label = "Uygulama bilgisi") { currentOnAppInfo() },
        )
    }

    Box(modifier = modifier) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth(),
        ) {
            AppIcon(
                app = app,
                onClick = onOpen,
                onLongClick = { menuOpen = true },
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = app.label,
                style = LabelStyle,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        AppContextMenu(
            expanded = menuOpen,
            onDismiss = { menuOpen = false },
            actions = actions,
        )
    }
}
