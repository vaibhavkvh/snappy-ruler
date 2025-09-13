package com.example.snappyruller.ui

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.*

@Composable
fun CanvasView(vm: CanvasViewModel, modifier: Modifier = Modifier) {
    val state by vm.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current

    // simple zoom & pan state
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    Box(modifier = modifier) {
        // Canvas
        Canvas(modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoomChange, rotationChange ->
                    offset += pan
                    scale *= zoomChange
                    scale = scale.coerceIn(0.5f, 4f)
                    vm.updateZoom(scale)

                    // Only apply rotation if TWO fingers are active
                  //  if (currentEvent.changes.size == 2) {
                        vm.rotateActiveTool(Math.toDegrees(rotationChange.toDouble()).toFloat())
                  //  }
                }
            }

            .pointerInput(state.selectedTool) {
                if (state.selectedTool == Tool.PEN) {
                    detectDragGestures(
                        onDragStart = { vm.startPenStroke(it) },
                        onDrag = { change, _ -> vm.continuePenStroke(change.position) },
                        onDragEnd = { vm.endPenStroke() }
                    )
                } else if (state.selectedTool == Tool.RULER) {
                    detectDragGestures(
                        onDragStart = { vm.startRulerLine(it) },
                        onDrag = { change, _ -> vm.continueRulerLine(change.position) },
                        onDragEnd = { vm.endRulerLine() }
                    )
                } else if (state.selectedTool == Tool.SET_SQUARE_45 || state.selectedTool == Tool.SET_SQUARE_30_60) {
                    detectDragGestures(
                        onDragStart = { vm.startSetSquareLine(it) },
                        onDrag = { change, _ -> vm.continueSetSquareLine(change.position) },
                        onDragEnd = { vm.endSetSquareLine() }
                    )
                } else {
                    detectTapGestures(
                        onLongPress = { vm.toggleSnapTemporary() },
                        onTap = { pos -> vm.onCanvasTap(pos, offset, scale) }
                    )
                }
            }
        ) {
            // background
            drawRect(Color(0xFFFAFAFA))
            // grid
            val gridSpacingPx = state.gridSpacingPx * scale
            if (gridSpacingPx > 6f) {
                val w = size.width
                val h = size.height
                var x = offset.x % gridSpacingPx
                while (x < w) {
                    drawLine(Color(0xFFEFEFEF), Offset(x, 0f), Offset(x, h), strokeWidth = 1f)
                    x += gridSpacingPx
                }
                var y = offset.y % gridSpacingPx
                while (y < h) {
                    drawLine(Color(0xFFEFEFEF), Offset(0f, y), Offset(w, y), strokeWidth = 1f)
                    y += gridSpacingPx
                }
            }

            // draw existing shapes
            state.shapes.forEach { shape ->
                when (shape) {
                    is Shape.Line -> drawLineShape(shape)
                    is Shape.Circle -> drawCircleShape(shape)
                    is Shape.PathShape -> drawPathShape(shape)
                }
            }

            // draw active pen path
            state.currentPath?.let { pathShape ->
                drawPathShape(pathShape)
            }
            vm.tempSetSquareLine?.let { drawLineShape(it) }


            vm.tempLine?.let { drawLineShape(it) }

            // draw ruler extents & handle
            state.activeToolInstance?.let { ti ->


                when (ti) {
                    is ToolInstance.RulerInstance -> {
                        val p = ti.position
                        withTransform({
                            translate(p.x, p.y)
                            rotate(ti.rotation)
                        }) {
                            val w = ti.length
                            val h = ti.thickness
                            drawRect(
                                color = Color(0xFFDDDDFF),
                                topLeft = Offset(-w / 2, -h / 2),
                                size = Size(w, h)
                            )
                            drawLine(
                                Color.Black,
                                Offset(-w / 2, 0f),
                                Offset(w / 2, 0f),
                                strokeWidth = 2f
                            )
                        }
                    }

                    is ToolInstance.ProtractorInstance -> TODO()
                    is ToolInstance.SetSquareInstance -> {
                        val p = ti.position
                        withTransform({
                            translate(p.x, p.y)
                            rotate(ti.angle)
                        }) {
                            val size = 200f
                            val path = androidx.compose.ui.graphics.Path().apply {
                                moveTo(0f, 0f)
                                lineTo(size, 0f)
                                lineTo(0f, size)
                                close()
                            }
                            drawPath(path, Color(0xFFDDFFDD))
                            drawPath(path, Color.Black, style = Stroke(width = 2f))
                        }
                    }
                }

            }

            // HUD: angle/length overlay
            state.precisionHud?.let { hud ->
                drawContext.canvas.nativeCanvas.apply {
                    drawText(hud.text, 10f, 30f, android.graphics.Paint().apply {
                        textSize = 36f
                        color = android.graphics.Color.BLACK
                    })
                }
            }
        }

        // small floating card for status & export result
        state.exportResultUri?.let { uri ->
            Card(modifier = Modifier
                .padding(12.dp)
                .width(200.dp)) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text("Export saved")
                    Text(uri.toString(), maxLines = 2)
                }
            }
        }
    }
}

// helper draw functions
private fun DrawScope.drawLineShape(line: Shape.Line) {
    drawLine(
        color = Color.Black,
        start = line.start,
        end = line.end,
        strokeWidth = 3f
    )
}
private fun DrawScope.drawCircleShape(circle: Shape.Circle) {
    drawCircle(Color.Transparent, radius = circle.radius, center = circle.center, style = androidx.compose.ui.graphics.drawscope.Fill)
    drawCircle(Color.Black, radius = circle.radius, center = circle.center, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f))
}
private fun DrawScope.drawPathShape(pathShape: Shape.PathShape) {
    val p = Path().apply {
        if (pathShape.points.isNotEmpty()) {
            moveTo(pathShape.points.first().x, pathShape.points.first().y)
            pathShape.points.drop(1).forEach { lineTo(it.x, it.y) }
        }
    }
    drawPath(path = p, color = Color.Black, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3f))
}