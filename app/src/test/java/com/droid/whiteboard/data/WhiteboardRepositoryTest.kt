package com.droid.whiteboard.data

import com.droid.whiteboard.model.Point
import com.droid.whiteboard.model.Stroke
import com.droid.whiteboard.model.Whiteboard
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File
import java.nio.file.Files

class WhiteboardRepositoryTest {

    private lateinit var tempDir: File
    private lateinit var repository: WhiteboardRepository

    @Before
    fun setup() {
        tempDir = Files.createTempDirectory("whiteboard_test").toFile()
        repository = FileWhiteboardRepository(tempDir)
    }

    @After
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun initialEmptyState_createsDefaultBoard() = runTest {
        val boards = repository.getAllWhiteboards()
        assertEquals(1, boards.size)
        assertTrue(boards[0].isDarkMode)

        val activeBoard = repository.getWhiteboard(boards[0].id)
        assertNotNull(activeBoard)
        assertEquals(boards[0].id, activeBoard?.id)
        assertTrue(activeBoard!!.isDarkMode)
    }

    @Test
    fun createSaveAndLoadWhiteboard_preservesStrokesAndMetadata() = runTest {
        val initialBoards = repository.getAllWhiteboards()
        assertEquals(1, initialBoards.size)

        val newBoard = repository.createNewWhiteboard(title = "Design Sketch", isDarkMode = true)
        val stroke = Stroke(
            points = listOf(Point(10f, 20f), Point(30f, 40f)),
            colorHex = 0xFFFFFFFF,
            strokeWidth = 5f,
            isEraser = false
        )
        val updatedBoard = newBoard.copy(strokes = listOf(stroke))
        repository.saveWhiteboard(updatedBoard)

        val allBoards = repository.getAllWhiteboards()
        assertEquals(2, allBoards.size)

        val loaded = repository.getWhiteboard(newBoard.id)
        assertNotNull(loaded)
        assertEquals("Design Sketch", loaded?.title)
        assertTrue(loaded!!.isDarkMode)
        assertEquals(1, loaded.strokes.size)
        assertEquals(2, loaded.strokes[0].points.size)
        assertEquals(10f, loaded.strokes[0].points[0].x, 0.001f)
        assertEquals(20f, loaded.strokes[0].points[0].y, 0.001f)
    }

    @Test
    fun deleteWhiteboard_removesBoardAndFile() = runTest {
        // Ensure default board exists
        val initial = repository.getAllWhiteboards()
        assertEquals(1, initial.size)

        val board = repository.createNewWhiteboard(title = "To Delete", isDarkMode = false)
        val allBefore = repository.getAllWhiteboards()
        assertEquals(2, allBefore.size)

        val deleted = repository.deleteWhiteboard(board.id)
        assertTrue(deleted)

        val allAfter = repository.getAllWhiteboards()
        assertEquals(1, allAfter.size)
        assertFalse(allAfter.any { it.id == board.id })
        assertEquals(null, repository.getWhiteboard(board.id))
    }
}
