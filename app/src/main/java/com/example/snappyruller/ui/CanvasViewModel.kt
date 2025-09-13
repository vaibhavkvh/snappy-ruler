package com.example.snappyruller.ui


import android.content.Context
import android.net.Uri
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

data class CanvasUiState(
    val shapes: List<Shape> = emptyList(),
    val currentPath: Shape.PathShape? = null,
    val selectedTool: Tool = Tool.PEN,
    val activeToolInstance: ToolInstance? = null,
    val precisionHud: PrecisionHud? = null,
    val undoRedoStack: UndoRedoStack = UndoRedoStack(),
    val exportResultUri: Uri? = null,
    val gridSpacingPx: Float = 50f, // for 5mm at assumed density
    val snapEnabled: Boolean = true
)

class CanvasViewModel : ViewModel() {

    private var tempSqLine: Shape.Line? = null
    val tempSetSquareLine: Shape.Line? get() = tempSqLine
    private val _uiState = MutableStateFlow(CanvasUiState())
    val uiState: StateFlow<CanvasUiState> = _uiState

    private var zoom = 1f

    fun selectTool(tool: Tool) {
        val inst = when (tool) {
            Tool.RULER -> {
                ToolInstance.RulerInstance(
                    position = Offset(500f, 500f), // default center
                    length = 400f,
                    thickness = 20f,
                    rotation = 0f
                )
            }
            Tool.PROTRACTOR -> ToolInstance.ProtractorInstance(position = Offset(500f, 500f))
            Tool.SET_SQUARE_45 -> ToolInstance.SetSquareInstance(
                position = Offset(350f, 200f),
                angle = 45f
            )

            Tool.SET_SQUARE_30_60 -> ToolInstance.SetSquareInstance(
                position = Offset(350f, 200f),
                angle = 30f
            )

            else -> null
        }
        _uiState.value = _uiState.value.copy(selectedTool = tool, activeToolInstance = inst)
    }

    fun updateZoom(s: Float) {
        zoom = s
    }

    fun updateTool(tool: ToolInstance) {
        _uiState.value = _uiState.value.copy(activeToolInstance = tool)
    }

    fun startPenStroke(pos: Offset) {
        val newPath = Shape.PathShape(points = mutableListOf(pos))
        _uiState.value = _uiState.value.copy(currentPath = newPath)
    }

    fun continuePenStroke(pos: Offset) {
        val cur = _uiState.value.currentPath ?: return
        cur.points.add(pos)
        val start = cur.points.first()
        val length = hypot((pos.x - start.x).toDouble(), (pos.y - start.y).toDouble())
        _uiState.value = _uiState.value.copy(
            precisionHud = PrecisionHud(text = "Len: ${"%.1f".format(length)} px"),
            currentPath = cur
        )
    }

    fun endPenStroke() {
        val cur = _uiState.value.currentPath ?: return
        val updatedShapes = _uiState.value.shapes + cur
        _uiState.value = _uiState.value.copy(
            shapes = updatedShapes,
            currentPath = null,
            precisionHud = null
        )
    }

    fun onCanvasTap(pos: Offset, canvasOffset: Offset, canvasScale: Float) {
        val tool = _uiState.value.selectedTool
        if (tool == Tool.RULER) {
            val inst = _uiState.value.activeToolInstance as? ToolInstance.RulerInstance ?: return
            val snapped = if (_uiState.value.snapEnabled) {
                Snapping.findBestSnap(pos, _uiState.value.shapes, canvasScale)
            } else pos
            val updated = inst.copy(position = snapped)
            _uiState.value = _uiState.value.copy(activeToolInstance = updated)
        }
    }

    fun toggleSnapTemporary() {
        _uiState.value = _uiState.value.copy(snapEnabled = !_uiState.value.snapEnabled)
    }

    fun undo() {
        val shapes = _uiState.value.shapes.toMutableList()
        if (shapes.isNotEmpty()) {
            shapes.removeAt(shapes.size - 1)
            _uiState.value = _uiState.value.copy(shapes = shapes)
        }
    }

    fun redo() {
        // TODO: implement redo stack
    }

    fun exportToImage(context: Context) {
        viewModelScope.launch {
            val uri = ExportUtil.saveCanvasBitmapToCache(context, "snappy_export.png")
            _uiState.value = _uiState.value.copy(exportResultUri = uri)
        }
    }

    fun startSetSquareLine(pos: Offset) {
        val inst = _uiState.value.activeToolInstance as? ToolInstance.SetSquareInstance ?: return
        val edges = getSetSquareEdgeDirections(inst)
        val best = edges.minByOrNull { (_, dir) ->
            angleDifference(dir, pos - inst.position)
        } ?: return
        tempSqLine = Shape.Line(pos, pos) // start where finger touches
        activeSqDir = best.second // remember which edge direction
    }
    private var activeSqDir: Offset? = null

    fun continueSetSquareLine(pos: Offset) {
        activeSqDir?.let { dir ->
            val start = tempSqLine?.start ?: pos
            val end = projectPointOntoLine(pos, start, start + dir * 200f)
            tempSqLine = Shape.Line(start, end)
        }
    }
  /*  fun continueSetSquareLine(pos: Offset) {
        val inst = _uiState.value.activeToolInstance as? ToolInstance.SetSquareInstance ?: return
        val edges = getSetSquareEdges(inst)
        val nearest =
            edges.minByOrNull { edge -> distanceToLine(pos, edge.first, edge.second) } ?: return
        tempSqLine = Shape.Line(nearest.first, pos)
    }
*/
    fun endSetSquareLine() {
        tempSqLine?.let { line ->
            _uiState.value = _uiState.value.copy(shapes = _uiState.value.shapes + line)
            tempSqLine = null
        }
    }

   /* fun startRulerLine(pos: Offset) {
        val inst = _uiState.value.activeToolInstance as? ToolInstance.RulerInstance ?: return
        // Project tap point onto ruler edge
        val dir = Offset(
            cos(Math.toRadians(inst.rotation.toDouble())).toFloat(),
            sin(Math.toRadians(inst.rotation.toDouble())).toFloat()
        )
        val start = inst.position + dir * ((pos - inst.position).dot(dir))
        val newLine = Shape.Line(start, start)
        _uiState.value = _uiState.value.copy(currentPath = null, precisionHud = null)
        tempLine = newLine
    }*/


    private var _tempLine: Shape.Line? = null
    val tempLine: Shape.Line? get() = _tempLine

   /* fun continueRulerLine(pos: Offset) {
        val inst = _uiState.value.activeToolInstance as? ToolInstance.RulerInstance ?: return
        val dir = Offset(
            cos(Math.toRadians(inst.rotation.toDouble())).toFloat(),
            sin(Math.toRadians(inst.rotation.toDouble())).toFloat()
        )
        val start = tempLine?.start ?: return
        val end = inst.position + dir * ((pos - inst.position).dot(dir))
        tempLine = Shape.Line(start, end)
        val length = hypot((end.x - start.x).toDouble(), (end.y - start.y).toDouble())
        _uiState.value =
            _uiState.value.copy(precisionHud = PrecisionHud("Len: ${"%.1f".format(length)} px"))
    }*/
    private fun getSetSquareEdges(inst: ToolInstance.SetSquareInstance): List<Pair<Offset, Offset>> {
        val size = 200f
        val a = inst.position
        val b = Offset(a.x + size, a.y)
        val c = Offset(a.x, a.y + size)
        return listOf(a to b, b to c, c to a)
    }

    private fun distanceToLine(p: Offset, a: Offset, b: Offset): Float {
        val ab = b - a
        val ap = p - a
        val t = (ap.dot(ab) / ab.dot(ab)).coerceIn(0f, 1f)
        val proj = a + ab * t
        return (p - proj).getDistance()
    }
 /*   fun endRulerLine() {
        tempLine?.let { line ->
            val updatedShapes = _uiState.value.shapes + line
            _uiState.value = _uiState.value.copy(shapes = updatedShapes, precisionHud = null)
            tempLine = null
        }
    }

    fun startRulerLine(pos: Offset) {
        val inst = _uiState.value.activeToolInstance as? ToolInstance.RulerInstance ?: return
        val dir = Offset(
            cos(Math.toRadians(inst.rotation.toDouble())).toFloat(),
            sin(Math.toRadians(inst.rotation.toDouble())).toFloat()
        )
        // Project pos onto ruler infinite line
        val start = projectPointOntoLine(pos, inst.position, inst.position + dir * 100f)
        tempLine = Shape.Line(start, start)
    }

    fun continueRulerLine(pos: Offset) {
        val inst = _uiState.value.activeToolInstance as? ToolInstance.RulerInstance ?: return
        val dir = Offset(
            cos(Math.toRadians(inst.rotation.toDouble())).toFloat(),
            sin(Math.toRadians(inst.rotation.toDouble())).toFloat()
        )
        val start = tempLine?.start ?: return
        val end = projectPointOntoLine(pos, inst.position, inst.position + dir * 100f)
        tempLine = Shape.Line(start, end)
    }*/

    private fun getSetSquareEdgeDirections(inst: ToolInstance.SetSquareInstance): List<Pair<Offset, Offset>> {
        val size = 200f
        // Define triangle points relative to (0,0)
        val a = Offset(0f, 0f)
        val b = Offset(size, 0f)
        val c = Offset(0f, size)

        // Rotate points by inst.angle
        fun rotate(p: Offset, deg: Float): Offset {
            val rad = Math.toRadians(deg.toDouble())
            val cos = cos(rad).toFloat()
            val sin = sin(rad).toFloat()
            return Offset(
                p.x * cos - p.y * sin,
                p.x * sin + p.y * cos
            )
        }

        val pa = inst.position + rotate(a, inst.angle)
        val pb = inst.position + rotate(b, inst.angle)
        val pc = inst.position + rotate(c, inst.angle)

        // Directions (normalized) of edges
        fun dir(p1: Offset, p2: Offset): Offset {
            val d = p2 - p1
            val len = d.getDistance().coerceAtLeast(1e-6f)
            return Offset(d.x / len, d.y / len)
        }

        return listOf(
            pa to dir(pa, pb),
            pb to dir(pb, pc),
            pc to dir(pc, pa)
        )
    }

    private fun angleDifference(a: Offset, b: Offset): Float {
        val dot = (a.x * b.x + a.y * b.y)
        val mag = a.getDistance() * b.getDistance()
        if (mag < 1e-6f) return 180f
        val cos = (dot / mag).coerceIn(-1f, 1f)
        return Math.toDegrees(acos(cos).toDouble()).toFloat()
    }


    fun startRulerLine(pos: Offset) {
        val inst = _uiState.value.activeToolInstance as? ToolInstance.RulerInstance ?: return
        val dir = Offset(
            cos(Math.toRadians(inst.rotation.toDouble())).toFloat(),
            sin(Math.toRadians(inst.rotation.toDouble())).toFloat()
        )
        // project touch onto ruler edge
        val start = projectPointOntoLine(pos, inst.position, inst.position + dir * 500f)
        _tempLine = Shape.Line(start, start)
    }

    fun continueRulerLine(pos: Offset) {
        val inst = _uiState.value.activeToolInstance as? ToolInstance.RulerInstance ?: return
        val dir = Offset(
            cos(Math.toRadians(inst.rotation.toDouble())).toFloat(),
            sin(Math.toRadians(inst.rotation.toDouble())).toFloat()
        )
        val start = _tempLine?.start ?: return
        val end = projectPointOntoLine(pos, inst.position, inst.position + dir * 500f)
        _tempLine = Shape.Line(start, end)
    }

    fun endRulerLine() {
        _tempLine?.let {
            _uiState.value = _uiState.value.copy(shapes = _uiState.value.shapes + it)
            _tempLine = null
        }
    }
    fun moveActiveTool(delta: Offset) {
        val tool = _uiState.value.activeToolInstance ?: return
        when (tool) {
            is ToolInstance.RulerInstance -> _uiState.value =
                _uiState.value.copy(activeToolInstance = tool.copy(position = tool.position + delta))
            is ToolInstance.SetSquareInstance -> _uiState.value =
                _uiState.value.copy(activeToolInstance = tool.copy(position = tool.position + delta))
            is ToolInstance.ProtractorInstance -> _uiState.value =
                _uiState.value.copy(activeToolInstance = tool.copy(position = tool.position + delta))
            else -> {}
        }
    }

    fun rotateActiveTool(deltaDegrees: Float) {
        val tool = _uiState.value.activeToolInstance ?: return
        when (tool) {
            is ToolInstance.RulerInstance -> _uiState.value =
                _uiState.value.copy(activeToolInstance = tool.copy(rotation = tool.rotation + deltaDegrees))
            is ToolInstance.SetSquareInstance -> _uiState.value =
                _uiState.value.copy(activeToolInstance = tool.copy(angle = tool.angle + deltaDegrees))
            else -> {}
        }
    }
}