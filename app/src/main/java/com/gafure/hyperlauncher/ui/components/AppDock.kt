package com.gafure.hyperlauncher.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.gafure.hyperlauncher.model.AppInfo
import kotlinx.collections.immutable.ImmutableList

private val DockShape = RoundedCornerShape(32.dp)

/** Fixed dock: four apps on a translucent rounded glass slab (single layer, no blur). */
@Composable
fun AppDock(
    apps: ImmutableList<AppInfo>,
    onAppClick: (AppInfo) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (apps.isEmpty()) return
    val isDark = isSystemInDarkTheme()
    val containerColor = remember(isDark) {
        Color.White.copy(alpha = if (isDark) 0.10f else 0.28f)
    }
    val borderColor = remember { Color.White.copy(alpha = 0.12f) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(DockShape)
            .background(containerColor)
            .border(width = 1.dp, color = borderColor, shape = DockShape)
            .padding(vertical = 10.dp, horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        apps.forEach { app ->
            AppIcon(
                app = app,
                size = 56.dp,
                onClick = { onAppClick(app) },
            )
        }
    }
}
