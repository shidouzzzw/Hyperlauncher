package com.gafure.hyperlauncher.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.delay

private val ClockShadow = Shadow(
    color = Color.Black.copy(alpha = 0.38f),
    offset = Offset(0f, 2f),
    blurRadius = 14f,
)

/** Hoisted styles: no TextStyle allocation and no theme lookup per tick. */
private val TimeStyle = TextStyle(
    fontFamily = FontFamily.SansSerif,
    fontWeight = FontWeight.Thin,
    fontSize = 84.sp,
    lineHeight = 88.sp,
    letterSpacing = (-3).sp,
    color = Color.White,
    shadow = ClockShadow,
)

private val DateStyle = TextStyle(
    fontFamily = FontFamily.SansSerif,
    fontWeight = FontWeight.Medium,
    fontSize = 15.sp,
    lineHeight = 20.sp,
    letterSpacing = 0.8.sp,
    color = Color.White.copy(alpha = 0.82f),
    shadow = ClockShadow,
)

/**
 * Left-aligned clock and date. The ticker sleeps until the next minute boundary, so this
 * subtree recomposes at most once a minute.
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
        Text(text = timeText, style = TimeStyle)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = dateText, style = DateStyle)
    }
}
