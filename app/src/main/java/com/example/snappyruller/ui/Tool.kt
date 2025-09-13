package com.example.snappyruller.ui

import androidx.compose.ui.geometry.Offset

enum class Tool {
    PEN, RULER, PROTRACTOR, SET_SQUARE_45, SET_SQUARE_30_60
}

operator fun Offset.plus(o: Offset) = Offset(x + o.x, y + o.y)
operator fun Offset.minus(o: Offset) = Offset(x - o.x, y - o.y)
operator fun Offset.times(s: Float) = Offset(x * s, y * s)
fun Offset.dot(o: Offset) = x * o.x + y * o.y
sealed class ToolInstance {
    data class RulerInstance(
        val position: Offset = Offset.Zero,
        val rotation: Float = 0f,
        val length: Float = 300f,
        val thickness: Float = 24f
    ) : ToolInstance()

    data class ProtractorInstance(val position: Offset = Offset.Zero, val rotation: Float = 0f) : ToolInstance()
    data class SetSquareInstance(val position: Offset = Offset.Zero, val angle: Float = 45f) : ToolInstance()
}