package com.example.snappyruller.ui

import androidx.compose.ui.geometry.Offset
import kotlin.math.*

/** Geometry helpers used by snapping and tool math **/

fun angleBetween(a: Offset, b: Offset, c: Offset): Double {
    // angle at b between ba and bc
    val v1x = a.x - b.x
    val v1y = a.y - b.y
    val v2x = c.x - b.x
    val v2y = c.y - b.y
    val ang1 = atan2(v1y.toDouble(), v1x.toDouble())
    val ang2 = atan2(v2y.toDouble(), v2x.toDouble())
    var d = Math.toDegrees((ang2 - ang1))
    if (d < 0) d += 360.0
    return d
}

fun distance(a: Offset, b: Offset): Float {
    return hypot(a.x - b.x, a.y - b.y)
}

fun projectPointOntoLine(p: Offset, a: Offset, b: Offset): Offset {
    val ax = a.x; val ay = a.y
    val bx = b.x; val by = b.y
    val apx = p.x - ax; val apy = p.y - ay
    val abx = bx - ax; val aby = by - ay
    val abLen2 = abx*abx + aby*aby
    val t = ((apx*abx + apy*aby) / max(1e-6f, abLen2))
    return Offset(ax + abx*t, ay + aby*t)
}