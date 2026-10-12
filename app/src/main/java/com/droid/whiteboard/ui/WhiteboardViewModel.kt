package com.droid.whiteboard.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.droid.whiteboard.data.WhiteboardRepository
import com.droid.whiteboard.model.Point
import com.droid.whiteboard.model.Stroke
import com.droid.whiteboard.model.Whiteboard
import com.droid.whiteboard.model.WhiteboardMetadata
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class WhiteboardUiState(
    val isLoading: Boolean = true,
    val currentWhiteboard: Whiteboard = Whiteboard(
        id = "",
        title = "",
        createdAt = 0L,
        updatedAt = 0L,
        isDarkMode = true,
        strokes = emptyList()
    ),
    val strokes: List<Stroke> = emptyList(),
    val currentPoints: List<Point> = emptyList(),
    val undoStack: List<List<Stroke>> = emptyList(),
    val redoStack: List<List<Stroke>> = emptyList(),
    val savedBoards: List<WhiteboardMetadata> = emptyList(),
    val selectedColorHex: Long = 0xFFFFFFFF,
    val selectedStrokeWidth: Float = 6f,
    val isEraser: Boolean = false,
    val showBoardsSheet: Boolean = false,
    val showNewBoardDialog: Boolean = false,
    val showRenameDialog: Boolean = false,
    val userMessage: String? = null
) {
    val canUndo: Boolean get() = undoStack.isNotEmpty() || strokes.isNotEmpty()
    val canRedo: Boolean get() = redoStack.isNotEmpty()
}

class WhiteboardViewModel(
    private val repository: WhiteboardRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WhiteboardUiState())
    val uiState: StateFlow<WhiteboardUiState> = _uiState.asStateFlow()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val boards = repository.getAllWhiteboards()
            if (boards.isNotEmpty()) {
                val activeMeta = boards.first()
                val fullBoard = repository.getWhiteboard(activeMeta.id)
                if (fullBoard != null) {
                    val defaultMarkerColor = if (fullBoard.isDarkMode) 0xFFFFFFFF else 0xFF1E1E1E
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            currentWhiteboard = fullBoard,
                            strokes = fullBoard.strokes,
                            savedBoards = boards,
                            selectedColorHex = defaultMarkerColor,
                            undoStack = emptyList(),
                            redoStack = emptyList()
                        )
                    }
                    return@launch
                }
            }
            _uiState.update { it.copy(isLoading = false, savedBoards = boards) }
        }
    }

    fun onDrawStart(point: Point) {
        _uiState.update { it.copy(currentPoints = listOf(point)) }
    }

    fun onDrawMove(point: Point) {
        _uiState.update { it.copy(currentPoints = it.currentPoints + point) }
    }

    fun onDrawEnd() {
        val points = _uiState.value.currentPoints
        if (points.isNotEmpty()) {
            val stroke = Stroke(
                points = points,
                colorHex = if (_uiState.value.isEraser) {
                    if (_uiState.value.currentWhiteboard.isDarkMode) 0xFF000000 else 0xFFFFFFFF
                } else {
                    _uiState.value.selectedColorHex
                },
                strokeWidth = _uiState.value.selectedStrokeWidth,
                isEraser = _uiState.value.isEraser
            )
            onStrokeCompleted(stroke)
        }
        _uiState.update { it.copy(currentPoints = emptyList()) }
    }

    fun onStrokeCompleted(stroke: Stroke) {
        _uiState.update { state ->
            val newStrokes = state.strokes + stroke
            state.copy(
                strokes = newStrokes,
                undoStack = state.undoStack + listOf(state.strokes),
                redoStack = emptyList()
            )
        }
    }

    fun undo() {
        _uiState.update { state ->
            if (state.strokes.isEmpty() && state.undoStack.isEmpty()) return@update state

            val previousStrokes = if (state.undoStack.isNotEmpty()) {
                state.undoStack.last()
            } else {
                emptyList()
            }
            val newUndoStack = if (state.undoStack.isNotEmpty()) {
                state.undoStack.dropLast(1)
            } else {
                emptyList()
            }

            state.copy(
                strokes = previousStrokes,
                undoStack = newUndoStack,
                redoStack = state.redoStack + listOf(state.strokes)
            )
        }
    }

    fun redo() {
        _uiState.update { state ->
            if (state.redoStack.isEmpty()) return@update state

            val nextStrokes = state.redoStack.last()
            val newRedoStack = state.redoStack.dropLast(1)

            state.copy(
                strokes = nextStrokes,
                undoStack = state.undoStack + listOf(state.strokes),
                redoStack = newRedoStack
            )
        }
    }

    fun clearBoard() {
        _uiState.update { state ->
            if (state.strokes.isEmpty()) return@update state
            state.copy(
                strokes = emptyList(),
                undoStack = state.undoStack + listOf(state.strokes),
                redoStack = emptyList(),
                userMessage = "Board cleared"
            )
        }
    }

    fun toggleDarkMode() {
        _uiState.update { state ->
            val newDarkMode = !state.currentWhiteboard.isDarkMode
            val updatedBoard = state.currentWhiteboard.copy(isDarkMode = newDarkMode)
            val newSelectedColor = if (newDarkMode) 0xFFFFFFFF else 0xFF1E1E1E
            state.copy(
                currentWhiteboard = updatedBoard,
                selectedColorHex = newSelectedColor,
                isEraser = false
            )
        }
        saveCurrentWhiteboard(silent = true)
    }

    fun selectColor(colorHex: Long) {
        _uiState.update { it.copy(selectedColorHex = colorHex, isEraser = false) }
    }

    fun setStrokeWidth(width: Float) {
        _uiState.update { it.copy(selectedStrokeWidth = width) }
    }

    fun setEraserMode(enabled: Boolean) {
        _uiState.update { it.copy(isEraser = enabled) }
    }

    fun toggleEraser() {
        _uiState.update { it.copy(isEraser = !it.isEraser) }
    }

    fun saveCurrentWhiteboard(silent: Boolean = false) {
        viewModelScope.launch {
            val state = _uiState.value
            val boardToSave = state.currentWhiteboard.copy(
                strokes = state.strokes,
                updatedAt = System.currentTimeMillis()
            )
            val success = repository.saveWhiteboard(boardToSave)
            if (success) {
                val updatedList = repository.getAllWhiteboards()
                _uiState.update {
                    it.copy(
                        currentWhiteboard = boardToSave,
                        savedBoards = updatedList,
                        userMessage = if (silent) null else "Whiteboard saved successfully!"
                    )
                }
            } else if (!silent) {
                _uiState.update { it.copy(userMessage = "Failed to save whiteboard") }
            }
        }
    }

    fun createNewWhiteboard(title: String, isDarkMode: Boolean = true) {
        viewModelScope.launch {
            // Auto-save current board before switching
            val current = _uiState.value.currentWhiteboard
            if (current.id.isNotBlank()) {
                repository.saveWhiteboard(current.copy(strokes = _uiState.value.strokes))
            }

            val newBoard = repository.createNewWhiteboard(title = title, isDarkMode = isDarkMode)
            val allBoards = repository.getAllWhiteboards()
            val defaultColor = if (isDarkMode) 0xFFFFFFFF else 0xFF1E1E1E

            _uiState.update {
                it.copy(
                    currentWhiteboard = newBoard,
                    strokes = emptyList(),
                    undoStack = emptyList(),
                    redoStack = emptyList(),
                    savedBoards = allBoards,
                    selectedColorHex = defaultColor,
                    isEraser = false,
                    showNewBoardDialog = false,
                    showBoardsSheet = false,
                    userMessage = "Created \"${newBoard.title}\""
                )
            }
        }
    }

    fun switchWhiteboard(id: String) {
        viewModelScope.launch {
            // Auto-save current board first
            val current = _uiState.value.currentWhiteboard
            if (current.id.isNotBlank() && current.id != id) {
                repository.saveWhiteboard(current.copy(strokes = _uiState.value.strokes))
            }

            val target = repository.getWhiteboard(id)
            if (target != null) {
                val allBoards = repository.getAllWhiteboards()
                val defaultColor = if (target.isDarkMode) 0xFFFFFFFF else 0xFF1E1E1E
                _uiState.update {
                    it.copy(
                        currentWhiteboard = target,
                        strokes = target.strokes,
                        undoStack = emptyList(),
                        redoStack = emptyList(),
                        savedBoards = allBoards,
                        selectedColorHex = defaultColor,
                        isEraser = false,
                        showBoardsSheet = false,
                        userMessage = "Opened \"${target.title}\""
                    )
                }
            }
        }
    }

    fun deleteWhiteboard(id: String) {
        viewModelScope.launch {
            val success = repository.deleteWhiteboard(id)
            if (success) {
                val remaining = repository.getAllWhiteboards()
                if (_uiState.value.currentWhiteboard.id == id) {
                    val nextBoard = if (remaining.isNotEmpty()) {
                        repository.getWhiteboard(remaining.first().id)
                    } else null

                    if (nextBoard != null) {
                        _uiState.update {
                            it.copy(
                                currentWhiteboard = nextBoard,
                                strokes = nextBoard.strokes,
                                undoStack = emptyList(),
                                redoStack = emptyList(),
                                savedBoards = remaining,
                                userMessage = "Whiteboard deleted"
                            )
                        }
                    } else {
                        // Create a new board if none left
                        createNewWhiteboard("My Whiteboard", isDarkMode = true)
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            savedBoards = remaining,
                            userMessage = "Whiteboard deleted"
                        )
                    }
                }
            }
        }
    }

    fun renameCurrentWhiteboard(newTitle: String) {
        if (newTitle.isBlank()) return
        viewModelScope.launch {
            val updated = _uiState.value.currentWhiteboard.copy(
                title = newTitle,
                strokes = _uiState.value.strokes
            )
            repository.saveWhiteboard(updated)
            val allBoards = repository.getAllWhiteboards()
            _uiState.update {
                it.copy(
                    currentWhiteboard = updated,
                    savedBoards = allBoards,
                    showRenameDialog = false,
                    userMessage = "Renamed to \"$newTitle\""
                )
            }
        }
    }

    fun showBoardsSheet(show: Boolean) {
        _uiState.update { it.copy(showBoardsSheet = show) }
    }

    fun showNewBoardDialog(show: Boolean) {
        _uiState.update { it.copy(showNewBoardDialog = show) }
    }

    fun showRenameDialog(show: Boolean) {
        _uiState.update { it.copy(showRenameDialog = show) }
    }

    fun dismissUserMessage() {
        _uiState.update { it.copy(userMessage = null) }
    }
}
