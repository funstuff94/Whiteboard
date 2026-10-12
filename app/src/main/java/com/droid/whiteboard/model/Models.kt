package com.droid.whiteboard.model

data class Point(
    val x: Float,
    val y: Float
)

data class Stroke(
    val points: List<Point>,
    val colorHex: Long,
    val strokeWidth: Float,
    val isEraser: Boolean = false
)

data class Whiteboard(
    val id: String,
    val title: String,
    val createdAt: Long,
    val updatedAt: Long,
    val isDarkMode: Boolean = true,
    val strokes: List<Stroke> = emptyList()
)

data class WhiteboardMetadata(
    val id: String,
    val title: String,
    val createdAt: Long,
    val updatedAt: Long,
    val isDarkMode: Boolean,
    val strokeCount: Int
)
