package com.gafure.hyperlauncher.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.delay

private val ClockShadow = Shadow(
    color = Color.Black.copy(alpha = 0.38f),
    offset = Offset(0f, 2f),
    blurRadius = 14f,
)

/**
 * Left-aligned clock and date. The ticker sleeps until the next minute boundary instead of
 * waking every second, so it never re-renders more than once a minute.
 */
@Composable
fun ClockSection(modifier: Modifier = Modifier) {
    var now by remember { mutableStateOf(LocalDateTime.now()) }
    val timeFormatter = remember { DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault()) }
    val dateFormatter = remember {
        DateTimeFormatter.ofPattern("d MMMM EEEE", Locale.getDefault())
    }

    LaunchedEffect(Unit) {
        while (true) {
            now = LocalDateTime.now()
            val millisIntoMinute = System.currentTimeMillis() % 60_000L
            delay(60_000L - millisIntoMinute + 50L)
        }
    }

    val timeText = remember(now) { now.format(timeFormatter) }
    val dateText = remember(now) { now.format(dateFormatter) }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.Start,
    ) {
        Text(
            text = timeText,
            style = MaterialTheme.typography.displayLarge.copy(
                color = Color.White,
                shadow = ClockShadow,
            ),
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = dateText,
            style = MaterialTheme.typography.titleMedium.copy(
                color = Color.White.copy(alpha = 0.82f),
                fontWeight = FontWeight.Medium,
                shadow = ClockShadow,
            ),
        )
    }
}
