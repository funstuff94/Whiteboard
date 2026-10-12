package com.droid.whiteboard.ui

import com.droid.whiteboard.data.WhiteboardRepository
import com.droid.whiteboard.model.Point
import com.droid.whiteboard.model.Stroke
import com.droid.whiteboard.model.Whiteboard
import com.droid.whiteboard.model.WhiteboardMetadata
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class WhiteboardViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeRepository: FakeWhiteboardRepository
    private lateinit var viewModel: WhiteboardViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeWhiteboardRepository()
        viewModel = WhiteboardViewModel(fakeRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialState_loadsDefaultBoardInDarkMode() = runTest(testDispatcher) {
        testDispatcher.scheduler.advanceUntilIdle()
        val state = viewModel.uiState.value
        assertEquals("Initial Board", state.currentWhiteboard.title)
        assertTrue(state.currentWhiteboard.isDarkMode)
        assertTrue(state.strokes.isEmpty())
        assertFalse(state.canUndo)
        assertFalse(state.canRedo)
        assertEquals(0xFFFFFFFF, state.selectedColorHex)
    }

    @Test
    fun gestureDrawingFlow_commitsStrokeOnEnd() = runTest(testDispatcher) {
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.onDrawStart(Point(10f, 10f))
        assertEquals(1, viewModel.uiState.value.currentPoints.size)

        viewModel.onDrawMove(Point(20f, 20f))
        assertEquals(2, viewModel.uiState.value.currentPoints.size)

        viewModel.onDrawEnd()
        assertEquals(0, viewModel.uiState.value.currentPoints.size)
        assertEquals(1, viewModel.uiState.value.strokes.size)
        assertTrue(viewModel.uiState.value.canUndo)
    }

    @Test
    fun addStroke_undo_redo_cyclesCorrectly() = runTest(testDispatcher) {
        testDispatcher.scheduler.advanceUntilIdle()
        val stroke1 = Stroke(listOf(Point(0f, 0f)), 0xFFFFFFFF, 4f)
        val stroke2 = Stroke(listOf(Point(10f, 10f)), 0xFF38BDF8, 8f)

        viewModel.onStrokeCompleted(stroke1)
        viewModel.onStrokeCompleted(stroke2)
        assertEquals(2, viewModel.uiState.value.strokes.size)

        viewModel.undo()
        assertEquals(1, viewModel.uiState.value.strokes.size)
        assertTrue(viewModel.uiState.value.canRedo)

        viewModel.undo()
        assertEquals(0, viewModel.uiState.value.strokes.size)
        assertFalse(viewModel.uiState.value.canUndo)

        viewModel.redo()
        assertEquals(1, viewModel.uiState.value.strokes.size)

        viewModel.redo()
        assertEquals(2, viewModel.uiState.value.strokes.size)
    }

    @Test
    fun clearBoard_removesAllStrokesAndAllowsUndo() = runTest(testDispatcher) {
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.onStrokeCompleted(Stroke(listOf(Point(1f, 1f)), 0xFFFFFFFF, 6f))
        assertEquals(1, viewModel.uiState.value.strokes.size)

        viewModel.clearBoard()
        assertEquals(0, viewModel.uiState.value.strokes.size)
        assertTrue(viewModel.uiState.value.canUndo)

        viewModel.undo()
        assertEquals(1, viewModel.uiState.value.strokes.size)
    }

    @Test
    fun toolAndPaletteSettings_updateCorrectly() = runTest(testDispatcher) {
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.selectColor(0xFF4ADE80)
        assertEquals(0xFF4ADE80, viewModel.uiState.value.selectedColorHex)
        assertFalse(viewModel.uiState.value.isEraser)

        viewModel.setStrokeWidth(16f)
        assertEquals(16f, viewModel.uiState.value.selectedStrokeWidth)

        viewModel.setEraserMode(true)
        assertTrue(viewModel.uiState.value.isEraser)

        viewModel.toggleEraser()
        assertFalse(viewModel.uiState.value.isEraser)
    }

    @Test
    fun renameAndSaveWhiteboard_updatesMetadata() = runTest(testDispatcher) {
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.renameCurrentWhiteboard("Updated Math Notes")
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("Updated Math Notes", viewModel.uiState.value.currentWhiteboard.title)

        viewModel.saveCurrentWhiteboard()
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals("Whiteboard saved successfully!", viewModel.uiState.value.userMessage)

        viewModel.dismissUserMessage()
        assertNull(viewModel.uiState.value.userMessage)
    }

    @Test
    fun dialogStateToggles() = runTest(testDispatcher) {
        viewModel.showBoardsSheet(true)
        assertTrue(viewModel.uiState.value.showBoardsSheet)
        viewModel.showBoardsSheet(false)
        assertFalse(viewModel.uiState.value.showBoardsSheet)

        viewModel.showNewBoardDialog(true)
        assertTrue(viewModel.uiState.value.showNewBoardDialog)
        viewModel.showNewBoardDialog(false)
        assertFalse(viewModel.uiState.value.showNewBoardDialog)

        viewModel.showRenameDialog(true)
        assertTrue(viewModel.uiState.value.showRenameDialog)
        viewModel.showRenameDialog(false)
        assertFalse(viewModel.uiState.value.showRenameDialog)
    }

    @Test
    fun deleteWhiteboard_switchesOrRecreatesBoard() = runTest(testDispatcher) {
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.createNewWhiteboard("Second Board", isDarkMode = false)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(2, viewModel.uiState.value.savedBoards.size)
        val secondBoardId = viewModel.uiState.value.currentWhiteboard.id

        viewModel.deleteWhiteboard(secondBoardId)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.savedBoards.size)
        assertEquals("Initial Board", viewModel.uiState.value.currentWhiteboard.title)
    }
}

class FakeWhiteboardRepository : WhiteboardRepository {
    private val boards = mutableMapOf<String, Whiteboard>()

    init {
        val initial = Whiteboard(
            id = "board-1",
            title = "Initial Board",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            isDarkMode = true,
            strokes = emptyList()
        )
        boards[initial.id] = initial
    }

    override suspend fun getAllWhiteboards(): List<WhiteboardMetadata> {
        return boards.values.map {
            WhiteboardMetadata(
                id = it.id,
                title = it.title,
                createdAt = it.createdAt,
                updatedAt = it.updatedAt,
                isDarkMode = it.isDarkMode,
                strokeCount = it.strokes.size
            )
        }
    }

    override suspend fun getWhiteboard(id: String): Whiteboard? = boards[id]

    override suspend fun saveWhiteboard(whiteboard: Whiteboard): Boolean {
        boards[whiteboard.id] = whiteboard
        return true
    }

    override suspend fun deleteWhiteboard(id: String): Boolean {
        return boards.remove(id) != null
    }

    override suspend fun createNewWhiteboard(title: String, isDarkMode: Boolean): Whiteboard {
        val id = "board-${System.currentTimeMillis()}-${boards.size}"
        val board = Whiteboard(
            id = id,
            title = title.ifBlank { "Untitled" },
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            isDarkMode = isDarkMode,
            strokes = emptyList()
        )
        boards[id] = board
        return board
    }
}
