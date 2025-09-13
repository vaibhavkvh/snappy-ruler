package com.example.snappyruller.ui

import androidx.compose.ui.geometry.Offset
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.roundToInt

object Snapping {
    private val angleSnaps = listOf(0, 30, 45, 60, 90, 120, 135, 150, 180)

    data class SnapResult(val pos: Offset, val snapType: String?, val angle: Float? = null)

    /**
     * Find close snap in shapes or to angle grid.
     * For simplicity, this checks:
     *  - nearest endpoint or midpoint of lines
     *  - nearest common angle by projecting to angle increments
     *
     * The snapRadius param should be dynamic depending on zoom; viewmodel computes that.
     */
    fun findBestSnap(pos: Offset, shapes: List<Shape>, zoom: Float, baseRadius: Float = 20f): Offset {
        val radius = (baseRadius / zoom).coerceAtLeast(8f)
        // check points (endpoints & midpoints)
        var best: Offset? = null
        var bestDist = Float.MAX_VALUE
        shapes.forEach { s ->
            when (s) {
                is Shape.Line -> {
                    val candidates = listOf(s.start, s.end, Offset((s.start.x + s.end.x)/2f, (s.start.y + s.end.y)/2f))
                    candidates.forEach { c ->
                        val d = hypot(c.x - pos.x, c.y - pos.y)
                        if (d < bestDist && d <= radius) {
                            bestDist = d; best = c
                        }
                    }
                }
                is Shape.Circle -> {
                    // snap to center
                    val d = hypot(s.center.x - pos.x, s.center.y - pos.y)
                    if (d < bestDist && d <= radius) {
                        bestDist = d; best = s.center
                    }
                }
                else -> {}
            }
        }

        if (best != null) return best!!

        // angle snapping: snap to nearest angle increment relative to horizontal
        val rawAngle = Math.toDegrees(kotlin.math.atan2(pos.y.toDouble(), pos.x.toDouble()))
        val normalized = ((rawAngle + 360) % 360)
        var nearestAngle = angleSnaps.minByOrNull { abs(it - normalized) } ?: 0
        // if within threshold, snap
        val angDiff = abs(nearestAngle - normalized)
        if (angDiff <= 6) {
            // snap by projecting along that angle keeping distance
            val dist = radius.coerceAtLeast(30f)
            val rad = Math.toRadians(nearestAngle.toDouble())
            val nx = pos.x + (dist * kotlin.math.cos(rad)).toFloat()
            val ny = pos.y + (dist * kotlin.math.sin(rad)).toFloat()
            return Offset(nx, ny)
        }

        // else return original
        return pos
    }
}