package com.example.snappyruller.ui

import androidx.compose.ui.geometry.Offset

sealed class Shape {
    data class Line(val start: Offset, val end: Offset) : Shape()
    data class Circle(val center: Offset, val radius: Float) : Shape()
    data class PathShape(val points: MutableList<Offset>) : Shape()
}

data class PrecisionHud(val text: String)