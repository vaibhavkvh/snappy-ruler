package com.example.snappyruller.ui

class UndoRedoStack {
    private val undo = ArrayDeque<() -> Unit>()
    private val redo = ArrayDeque<() -> Unit>()

    fun push(action: () -> Unit) {
        undo.addLast(action)
        redo.clear()
    }

    fun undoStep() {
        if (undo.isNotEmpty()) {
            val action = undo.removeLast()
            action()
            redo.addLast(action)
        }
    }

    fun redoStep() {
        if (redo.isNotEmpty()) {
            val action = redo.removeLast()
            action()
            undo.addLast(action)
        }
    }

    fun clear() {
        undo.clear(); redo.clear()
    }
}