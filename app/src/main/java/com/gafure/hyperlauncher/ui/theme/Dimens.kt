package com.gafure.hyperlauncher.ui.theme

import androidx.compose.ui.unit.dp

/**
 * Spacing and corner-radius tokens. HyperOS 4 readiness: new screens compose from these
 * values instead of hard-coded dp, so a future visual pass changes one file.
 */
object HyperSpacing {
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 24.dp
    val xxl = 32.dp
    val screenHorizontal = 20.dp
}

object HyperRadius {
    val sm = 12.dp
    val md = 18.dp
    val lg = 24.dp
    val xl = 32.dp
    val sheet = 30.dp
    val pill = 100.dp
}
