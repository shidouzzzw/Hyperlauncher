package com.gafure.hyperlauncher.ui.shape

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sign
import kotlin.math.sin

/**
 * Continuous-curvature squircle (superellipse) used for every icon mask, so apps with
 * different native masks still read as one consistent family.
 */
class SquircleShape(private val exponent: Float = SQUIRCLE_EXPONENT) : Shape {

    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density,
    ): Outline {
        if (size.width <= 0f || size.height <= 0f) {
            return Outline.Rectangle(Rect(0f, 0f, size.width, size.height))
        }
        val halfWidth = size.width / 2f
        val halfHeight = size.height / 2f
        val power = 2f / exponent
        val path = Path()
        for (step in 0..STEPS) {
            val angle = 2.0 * PI * step / STEPS
            val cosine = cos(angle).toFloat()
            val sine = sin(angle).toFloat()
            val x = halfWidth + halfWidth * sign(cosine) * abs(cosine).pow(power)
            val y = halfHeight + halfHeight * sign(sine) * abs(sine).pow(power)
            if (step == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        path.close()
        return Outline.Generic(path)
    }

    override fun equals(other: Any?): Boolean =
        other is SquircleShape && other.exponent == exponent

    override fun hashCode(): Int = exponent.hashCode()

    private companion object {
        const val SQUIRCLE_EXPONENT = 4.2f
        const val STEPS = 96
    }
}

/** Shared instance: one stable shape object keeps icon clipping allocation-free. */
val Squircle: Shape = SquircleShape()
