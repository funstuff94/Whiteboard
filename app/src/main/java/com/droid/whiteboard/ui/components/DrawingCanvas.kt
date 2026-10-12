package com.droid.whiteboard.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke as DrawStroke
import androidx.compose.ui.input.pointer.pointerInput
import com.droid.whiteboard.model.Point
import com.droid.whiteboard.model.Stroke

@Composable
fun DrawingCanvas(
    strokes: List<Stroke>,
    currentPoints: List<Point>,
    selectedColor: Color,
    selectedStrokeWidth: Float,
    isEraser: Boolean,
    isDarkMode: Boolean,
    onDrawStart: (Point) -> Unit,
    onDrawMove: (Point) -> Unit,
    onDrawEnd: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isDarkMode) Color(0xFF0F0F12) else Color(0xFFFBFBFB)

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        onDrawStart(Point(offset.x, offset.y))
                    },
                    onDrag = { change, _ ->
                        change.consume()
                        onDrawMove(Point(change.position.x, change.position.y))
                    },
                    onDragEnd = {
                        onDrawEnd()
                    },
                    onDragCancel = {
                        onDrawEnd()
                    }
                )
            }
    ) {
        // Draw completed strokes
        for (stroke in strokes) {
            if (stroke.points.isEmpty()) continue

            val strokeColor = if (stroke.isEraser) {
                backgroundColor
            } else {
                Color(stroke.colorHex)
            }

            if (stroke.points.size == 1) {
                val pt = stroke.points[0]
                drawCircle(
                    color = strokeColor,
                    radius = stroke.strokeWidth / 2f,
                    center = Offset(pt.x, pt.y)
                )
            } else {
                val path = Path()
                path.moveTo(stroke.points[0].x, stroke.points[0].y)
                for (i in 1 until stroke.points.size) {
                    val p0 = stroke.points[i - 1]
                    val p1 = stroke.points[i]
                    // Smooth quadratic bezier between points
                    val midX = (p0.x + p1.x) / 2f
                    val midY = (p0.y + p1.y) / 2f
                    path.quadraticTo(p0.x, p0.y, midX, midY)
                }
                val last = stroke.points.last()
                path.lineTo(last.x, last.y)

                drawPath(
                    path = path,
                    color = strokeColor,
                    style = DrawStroke(
                        width = stroke.strokeWidth,
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )
            }
        }

        // Draw in-progress stroke
        if (currentPoints.isNotEmpty()) {
            val liveColor = if (isEraser) backgroundColor else selectedColor

            if (currentPoints.size == 1) {
                val pt = currentPoints[0]
                drawCircle(
                    color = liveColor,
                    radius = selectedStrokeWidth / 2f,
                    center = Offset(pt.x, pt.y)
                )
            } else {
                val path = Path()
                path.moveTo(currentPoints[0].x, currentPoints[0].y)
                for (i in 1 until currentPoints.size) {
                    val p0 = currentPoints[i - 1]
                    val p1 = currentPoints[i]
                    val midX = (p0.x + p1.x) / 2f
                    val midY = (p0.y + p1.y) / 2f
                    path.quadraticTo(p0.x, p0.y, midX, midY)
                }
                val last = currentPoints.last()
                path.lineTo(last.x, last.y)

                drawPath(
                    path = path,
                    color = liveColor,
                    style = DrawStroke(
                        width = selectedStrokeWidth,
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )
            }
        }
    }
}
