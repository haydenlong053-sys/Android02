package com.lxkj.aiim.ui.shape

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

/**
 * A shape with a concave arc on the top edge.
 * @param arcHeightDp height of the arc in dp.
 */
class TopArcShape(private val arcHeightDp: Float = 24f) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val arc = with(density) { arcHeightDp.dp.toPx() }
        val w = size.width
        val h = size.height
        val path = Path().apply {
            // start at top-left corner
            moveTo(0f, 0f)
            // top concave arc from left to right (control point below the top edge)
            quadraticBezierTo(w / 2f, arc, w, 0f)
            // right side down
            lineTo(w, h)
            // bottom to left
            lineTo(0f, h)
            close()
        }
        return Outline.Generic(path)
    }
}

/**
 * A shape with a concave arc on the bottom edge.
 * @param arcHeightDp height of the arc in dp.
 */
class BottomArcShape(private val arcHeightDp: Float = 24f) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val arc = with(density) { arcHeightDp.dp.toPx() }
        val w = size.width
        val h = size.height
        val path = Path().apply {
            // top edge
            moveTo(0f, 0f)
            lineTo(w, 0f)
            // right side down to arc start
            lineTo(w, h - arc)
            // bottom arc from right to left with control point below
            quadraticBezierTo(w / 2f, h + arc, 0f, h - arc)
            // left side up
            lineTo(0f, 0f)
            close()
        }
        return Outline.Generic(path)
    }
}

/**
 * A shape with rounded top corners and a concave arc on the bottom edge.
 * @param topRadiusDp top corner radius in dp.
 * @param arcHeightDp height of the bottom concave arc in dp.
 */
class TopRoundedBottomArcShape(
    private val topRadiusDp: Float = 12f,
    private val arcHeightDp: Float = 16f
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val r = with(density) { topRadiusDp.dp.toPx() }
        val arc = with(density) { arcHeightDp.dp.toPx() }
        val w = size.width
        val h = size.height
        val path = Path().apply {
            // start at left, below the rounded corner
            moveTo(0f, r)
            // top-left rounded corner
            quadraticBezierTo(0f, 0f, r, 0f)
            // top edge to right before corner
            lineTo(w - r, 0f)
            // top-right rounded corner
            quadraticBezierTo(w, 0f, w, r)
            // right side down to start of bottom arc
            lineTo(w, h - arc)
            // bottom concave arc from right to left
            quadraticBezierTo(w / 2f, h + arc, 0f, h - arc)
            // left side up back to starting point
            lineTo(0f, r)
            close()
        }
        return Outline.Generic(path)
    }
}

/**
 * A shape with rounded top corners and a concave arc on the top edge.
 * @param topRadiusDp top corner radius in dp.
 * @param arcHeightDp height of the top concave arc in dp.
 */
class TopRoundedTopArcShape(
    private val topRadiusDp: Float = 12f,
    private val arcHeightDp: Float = 16f
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val r = with(density) { topRadiusDp.dp.toPx() }
        val arc = with(density) { arcHeightDp.dp.toPx() }
        val w = size.width
        val h = size.height
        val path = Path().apply {
            // start at left, below the rounded corner
            moveTo(0f, r)
            // top-left rounded corner
            quadraticBezierTo(0f, 0f, r, 0f)
            // top concave arc from left (after corner) to right (before corner)
            quadraticBezierTo(w / 2f, arc, w - r, 0f)
            // top-right rounded corner
            quadraticBezierTo(w, 0f, w, r)
            // right side down
            lineTo(w, h)
            // bottom edge to left
            lineTo(0f, h)
            // left side up back to starting point
            lineTo(0f, r)
            close()
        }
        return Outline.Generic(path)
    }
}