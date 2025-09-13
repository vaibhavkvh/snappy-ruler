package com.example.snappyruller.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp


@Composable
fun ToolPalette(vm: CanvasViewModel, modifier: Modifier = Modifier) {
    val context = LocalContext.current

    Surface(modifier = modifier) {
        Row(
            modifier = Modifier
                .padding(8.dp)
                .horizontalScroll(rememberScrollState())
        ) {
            Button(onClick = { vm.selectTool(Tool.PEN) }) { Text("Pen") }
            Button(onClick = { vm.selectTool(Tool.RULER) }) { Text("Ruler") }
            Button(onClick = { vm.selectTool(Tool.PROTRACTOR) }) { Text("Protractor") }
            Button(onClick = { vm.selectTool(Tool.SET_SQUARE_45) }) { Text("SetSq45") }
            Button(onClick = { vm.selectTool(Tool.SET_SQUARE_30_60) }) { Text("SetSq30/60") }
            Button(onClick = { vm.undo() }) { Text("Undo") }
            Button(onClick = { vm.redo() }) { Text("Redo") }
            Button(onClick = { vm.exportToImage(context) }) { Text("Export") }
        }
    }
}