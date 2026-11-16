package com.gafure.hyperlauncher.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.gafure.hyperlauncher.model.AppInfo
import com.gafure.hyperlauncher.ui.shape.Squircle

/**
 * A squircle-masked app icon that springs down to 86% while pressed and back on release.
 * Press state is local, so a tap never recomposes its neighbours.
 */
@Composable
fun AppIcon(
    app: AppInfo,
    contentDescription: String? = app.label,
    size: Dp = 54.dp,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    var pressed by remember(app.key) { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.86f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow,
        ),
        label = "appIconScale",
    )

    Image(
        bitmap = app.icon,
        contentDescription = contentDescription,
        contentScale = ContentScale.Fit,
        modifier = modifier
            .size(size)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(Squircle)
            .pointerInput(app.key) {
                detectTapGestures(
                    onPress = {
                        pressed = true
                        tryAwaitRelease()
                        pressed = false
                    },
                    onTap = { onClick() },
                )
            },
    )
}
