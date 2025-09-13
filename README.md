# 🧮 Snappy Ruler (Jetpack Compose)

Snappy Ruler is a Jetpack Compose-based Android drawing tool that combines precision with simplicity. It offers a canvas interface where users can draw, snap, and manipulate shapes with intuitive controls and undo support.

## ✅ Core Functionalities

| Feature        | Description |
|----------------|-------------|
| 🧭 Ruler        | Displays a snapping ruler for alignment and measurement |
| ✏️ Pen Tool     | Freehand drawing on the canvas |
| ◼️ Square 30°   | Draws a square rotated at 30 degrees |
| ◼️ Square 45°   | Draws a square rotated at 45 degrees |
| ↩️ Undo         | Reverts the last action using an undo stack |

These tools are accessible via a floating tool palette and are rendered using Jetpack Compose’s Canvas API.

## 🖼️ UI Overview

- **MainScreen.kt**: Hosts the canvas and tool palette
- **CanvasView.kt**: Custom composable for rendering shapes and drawings
- **ToolPalette.kt**: UI component for selecting tools
- **UndoRedoStack.kt**: Manages undo/redo history
- **Snapping.kt**: Implements snapping logic for ruler alignment

## 🧪 How It Works

- Select a tool from the palette (Pen, Square 30°, Square 45°)
- Draw directly on the canvas
- Use the ruler for precise placement
- Tap undo to revert the last action

## 🛠️ Tech Stack

- Kotlin
- Jetpack Compose
- MVVM Architecture
- Canvas API
- Material Design 3

## 📄 License

This project is licensed under the MIT License.
