package com.example.snappyruller.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel

/*@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(paddingValues: PaddingValues,vm: CanvasViewModel = viewModel()) {
    Scaffold(modifier = Modifier.padding(paddingValues),
        topBar = {
            CenterAlignedTopAppBar(title = { Text("Snappy Ruler Set") })
        },
        bottomBar = {
            ToolPalette(vm = vm, modifier = Modifier.fillMaxSize())
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            CanvasView(vm = vm, modifier = Modifier.fillMaxSize())
        }
    }
}*/

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(paddingValues: PaddingValues,vm: CanvasViewModel = viewModel()) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(title = { Text("Snappy Ruler Set") })
        },
        bottomBar = {
            ToolPalette(vm = vm, modifier = Modifier.fillMaxWidth())
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            CanvasView(vm = vm, modifier = Modifier.fillMaxSize())
        }
    }
}
