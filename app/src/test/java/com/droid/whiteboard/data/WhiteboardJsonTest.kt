package com.droid.whiteboard.data

import com.droid.whiteboard.model.Point
import com.droid.whiteboard.model.Stroke
import com.droid.whiteboard.model.Whiteboard
import com.droid.whiteboard.model.WhiteboardMetadata
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class WhiteboardJsonTest {

    @Test
    fun serializeAndParseIndex_maintainsAllFields() {
        val originalList = listOf(
            WhiteboardMetadata(
                id = "id-1",
                title = "Board One",
                createdAt = 1000L,
                updatedAt = 2000L,
                isDarkMode = true,
                strokeCount = 5
            ),
            WhiteboardMetadata(
                id = "id-2",
                title = "Board \"Two\" with quotes & special chars",
                createdAt = 3000L,
                updatedAt = 4000L,
                isDarkMode = false,
                strokeCount = 0
            )
        )

        val json = WhiteboardJson.indexToJson(originalList)
        val parsed = WhiteboardJson.parseIndex(json)

        assertEquals(2, parsed.size)
        assertEquals("id-1", parsed[0].id)
        assertEquals("Board One", parsed[0].title)
        assertEquals(1000L, parsed[0].createdAt)
        assertEquals(2000L, parsed[0].updatedAt)
        assertTrue(parsed[0].isDarkMode)
        assertEquals(5, parsed[0].strokeCount)

        assertEquals("id-2", parsed[1].id)
        assertEquals("Board \"Two\" with quotes & special chars", parsed[1].title)
        assertFalse(parsed[1].isDarkMode)
        assertEquals(0, parsed[1].strokeCount)
    }

    @Test
    fun serializeAndParseWhiteboard_maintainsStrokesAndPoints() {
        val board = Whiteboard(
            id = "test-board",
            title = "Complex Board\nWith Newlines",
            createdAt = 12345L,
            updatedAt = 67890L,
            isDarkMode = true,
            strokes = listOf(
                Stroke(
                    points = listOf(Point(1.5f, 2.5f), Point(3.5f, 4.5f)),
                    colorHex = 0xFF38BDF8,
                    strokeWidth = 8f,
                    isEraser = false
                ),
                Stroke(
                    points = listOf(Point(100f, 200f)),
                    colorHex = 0x00000000,
                    strokeWidth = 24f,
                    isEraser = true
                )
            )
        )

        val json = WhiteboardJson.whiteboardToJson(board)
        val parsed = WhiteboardJson.parseWhiteboard(json)

        assertNotNull(parsed)
        assertEquals("test-board", parsed?.id)
        assertEquals("Complex Board\nWith Newlines", parsed?.title)
        assertEquals(12345L, parsed?.createdAt)
        assertEquals(67890L, parsed?.updatedAt)
        assertTrue(parsed!!.isDarkMode)
        assertEquals(2, parsed.strokes.size)

        val s1 = parsed.strokes[0]
        assertEquals(0xFF38BDF8, s1.colorHex)
        assertEquals(8f, s1.strokeWidth, 0.001f)
        assertFalse(s1.isEraser)
        assertEquals(2, s1.points.size)
        assertEquals(1.5f, s1.points[0].x, 0.001f)
        assertEquals(2.5f, s1.points[0].y, 0.001f)

        val s2 = parsed.strokes[1]
        assertTrue(s2.isEraser)
        assertEquals(24f, s2.strokeWidth, 0.001f)
        assertEquals(1, s2.points.size)
    }
}
