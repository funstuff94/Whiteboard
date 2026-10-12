package com.droid.whiteboard.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.droid.whiteboard.ui.components.BoardsDialog
import com.droid.whiteboard.ui.components.BottomDrawingDock
import com.droid.whiteboard.ui.components.DrawingCanvas
import com.droid.whiteboard.ui.components.NewBoardDialog
import com.droid.whiteboard.ui.components.RenameBoardDialog
import com.droid.whiteboard.ui.components.TopWhiteboardBar

@Composable
fun WhiteboardScreen(
    viewModel: WhiteboardViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showClearConfirmation by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.userMessage) {
        val msg = uiState.userMessage
        if (msg != null) {
            snackbarHostState.showSnackbar(
                message = msg,
                duration = SnackbarDuration.Short
            )
            viewModel.dismissUserMessage()
        }
    }

    val isDarkMode = uiState.currentWhiteboard.isDarkMode

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopWhiteboardBar(
                boardTitle = uiState.currentWhiteboard.title,
                boardCount = uiState.savedBoards.size,
                isDarkMode = isDarkMode,
                canUndo = uiState.canUndo,
                canRedo = uiState.canRedo,
                onOpenBoards = { viewModel.showBoardsSheet(true) },
                onRenameBoard = { viewModel.showRenameDialog(true) },
                onNewBoard = { viewModel.showNewBoardDialog(true) },
                onToggleDarkMode = { viewModel.toggleDarkMode() },
                onUndo = { viewModel.undo() },
                onRedo = { viewModel.redo() },
                onSave = { viewModel.saveCurrentWhiteboard() },
                onClear = { showClearConfirmation = true }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Main Interactive Drawing Canvas
            DrawingCanvas(
                strokes = uiState.strokes,
                currentPoints = uiState.currentPoints,
                selectedColor = Color(uiState.selectedColorHex),
                selectedStrokeWidth = uiState.selectedStrokeWidth,
                isEraser = uiState.isEraser,
                isDarkMode = isDarkMode,
                onDrawStart = { pt -> viewModel.onDrawStart(pt) },
                onDrawMove = { pt -> viewModel.onDrawMove(pt) },
                onDrawEnd = { viewModel.onDrawEnd() }
            )

            // Floating Bottom Drawing Dock
            BottomDrawingDock(
                isDarkMode = isDarkMode,
                isEraser = uiState.isEraser,
                selectedColorHex = uiState.selectedColorHex,
                selectedStrokeWidth = uiState.selectedStrokeWidth,
                onSelectColor = { color -> viewModel.selectColor(color) },
                onToggleEraser = { viewModel.toggleEraser() },
                onSelectStrokeWidth = { width -> viewModel.setStrokeWidth(width) },
                modifier = Modifier.align(Alignment.BottomCenter)
            )

            // Dialog to Open / Manage Several Whiteboards
            if (uiState.showBoardsSheet) {
                BoardsDialog(
                    boards = uiState.savedBoards,
                    currentBoardId = uiState.currentWhiteboard.id,
                    isDarkMode = isDarkMode,
                    onSelectBoard = { id -> viewModel.switchWhiteboard(id) },
                    onDeleteBoard = { id -> viewModel.deleteWhiteboard(id) },
                    onNewBoardClick = {
                        viewModel.showBoardsSheet(false)
                        viewModel.showNewBoardDialog(true)
                    },
                    onDismiss = { viewModel.showBoardsSheet(false) }
                )
            }

            // Create New Board Dialog
            if (uiState.showNewBoardDialog) {
                NewBoardDialog(
                    isDarkModeInitial = isDarkMode,
                    onCreateBoard = { title, dark ->
                        viewModel.createNewWhiteboard(title, dark)
                    },
                    onDismiss = { viewModel.showNewBoardDialog(false) }
                )
            }

            // Rename Current Board Dialog
            if (uiState.showRenameDialog) {
                RenameBoardDialog(
                    currentTitle = uiState.currentWhiteboard.title,
                    onRename = { newTitle ->
                        viewModel.renameCurrentWhiteboard(newTitle)
                    },
                    onDismiss = { viewModel.showRenameDialog(false) }
                )
            }

            // Clear Board Confirmation Dialog
            if (showClearConfirmation) {
                AlertDialog(
                    onDismissRequest = { showClearConfirmation = false },
                    title = { Text("Clear Canvas") },
                    text = { Text("Are you sure you want to clear all drawings on this whiteboard?") },
                    confirmButton = {
                        Button(
                            onClick = {
                                viewModel.clearBoard()
                                showClearConfirmation = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                        ) {
                            Text("Clear", color = Color.White)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showClearConfirmation = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }
    }
}
