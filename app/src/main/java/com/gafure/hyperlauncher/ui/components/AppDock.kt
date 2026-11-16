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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.gafure.hyperlauncher.model.AppInfo

private val DockShape = RoundedCornerShape(32.dp)

/** Fixed dock: four apps on a translucent rounded glass slab. */
@Composable
fun AppDock(
    apps: List<AppInfo>,
    onAppClick: (AppInfo) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isDark = isSystemInDarkTheme()
    if (apps.isEmpty()) return

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(DockShape)
            .background(Color.White.copy(alpha = if (isDark) 0.10f else 0.28f))
            .border(width = 1.dp, color = Color.White.copy(alpha = 0.12f), shape = DockShape)
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
