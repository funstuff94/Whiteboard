package com.droid.whiteboard.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val DarkModeColors = listOf(
    0xFFFFFFFF, // Pure White
    0xFFFDE047, // Neon Yellow
    0xFF38BDF8, // Electric Cyan
    0xFF4ADE80, // Vivid Green
    0xFFF43F5E, // Coral Pink
    0xFFFB923C  // Orange
)

val LightModeColors = listOf(
    0xFF18181B, // Charcoal Black
    0xFF2563EB, // Royal Blue
    0xFFDC2626, // Crimson Red
    0xFF16A34A, // Forest Green
    0xFF9333EA, // Purple
    0xFFEA580C  // Sunset Orange
)

val StrokeWidthPresets = listOf(
    4f to "Fine",
    8f to "Medium",
    16f to "Bold",
    28f to "Thick"
)

@Composable
fun TopWhiteboardBar(
    boardTitle: String,
    boardCount: Int,
    isDarkMode: Boolean,
    canUndo: Boolean,
    canRedo: Boolean,
    onOpenBoards: () -> Unit,
    onRenameBoard: () -> Unit,
    onNewBoard: () -> Unit,
    onToggleDarkMode: () -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onSave: () -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    val barBackground = if (isDarkMode) Color(0xFF1E222A) else Color(0xFFF1F3F5)
    val contentColor = if (isDarkMode) Color(0xFFF1F5F9) else Color(0xFF1E293B)
    val subtitleColor = if (isDarkMode) Color(0xFF94A3B8) else Color(0xFF64748B)

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = barBackground,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: Board Selector Button with Title & Count
            Card(
                onClick = onOpenBoards,
                colors = CardDefaults.cardColors(
                    containerColor = if (isDarkMode) Color(0xFF282E39) else Color(0xFFE2E8F0)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = boardTitle.ifBlank { "Untitled" },
                                color = contentColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(text = "▼", fontSize = 10.sp, color = subtitleColor)
                        }
                        Text(
                            text = "$boardCount whiteboards saved",
                            color = subtitleColor,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            // Quick actions on title (Rename & New)
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onRenameBoard) {
                    Text(text = "✏️", fontSize = 18.sp)
                }

                IconButton(onClick = onNewBoard) {
                    Text(text = "➕", fontSize = 18.sp)
                }

                Spacer(Modifier.width(8.dp))

                // Dark / Light Mode Switcher
                Card(
                    onClick = onToggleDarkMode,
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDarkMode) Color(0xFF333B48) else Color(0xFFE2E8F0)
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isDarkMode) "🌙 Dark" else "☀️ Light",
                            color = contentColor,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            // Right: Actions (Undo, Redo, Save, Clear)
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onUndo, enabled = canUndo) {
                    Text(
                        text = "↩️",
                        fontSize = 18.sp,
                        color = if (canUndo) contentColor else subtitleColor.copy(alpha = 0.4f)
                    )
                }

                IconButton(onClick = onRedo, enabled = canRedo) {
                    Text(
                        text = "↪️",
                        fontSize = 18.sp,
                        color = if (canRedo) contentColor else subtitleColor.copy(alpha = 0.4f)
                    )
                }

                IconButton(onClick = onSave) {
                    Text(text = "💾", fontSize = 18.sp)
                }

                IconButton(onClick = onClear) {
                    Text(text = "🗑️", fontSize = 18.sp)
                }
            }
        }
    }
}

@Composable
fun BottomDrawingDock(
    isDarkMode: Boolean,
    isEraser: Boolean,
    selectedColorHex: Long,
    selectedStrokeWidth: Float,
    onSelectColor: (Long) -> Unit,
    onToggleEraser: () -> Unit,
    onSelectStrokeWidth: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val containerBg = if (isDarkMode) Color(0xEB1E222A) else Color(0xEBFFFFFF)
    val borderColor = if (isDarkMode) Color(0xFF374151) else Color(0xFFE2E8F0)
    val colors = if (isDarkMode) DarkModeColors else LightModeColors

    Card(
        modifier = modifier.padding(bottom = 16.dp),
        colors = CardDefaults.cardColors(containerColor = containerBg),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Tool Selector: Marker vs Eraser
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isDarkMode) Color(0xFF282E39) else Color(0xFFF1F5F9))
                    .padding(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ToolButton(
                    label = "✏️ Pen",
                    isSelected = !isEraser,
                    isDarkMode = isDarkMode,
                    onClick = { if (isEraser) onToggleEraser() }
                )
                Spacer(Modifier.width(4.dp))
                ToolButton(
                    label = "🧹 Eraser",
                    isSelected = isEraser,
                    isDarkMode = isDarkMode,
                    onClick = { if (!isEraser) onToggleEraser() }
                )
            }

            Box(
                modifier = Modifier
                    .height(28.dp)
                    .width(1.dp)
                    .background(borderColor)
            )

            // Color Palette
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                colors.forEach { colorHex ->
                    val isSelected = !isEraser && selectedColorHex == colorHex
                    val itemColor = Color(colorHex)

                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(itemColor)
                            .border(
                                width = if (isSelected) 3.dp else 1.dp,
                                color = if (isSelected) {
                                    if (isDarkMode) Color(0xFF38BDF8) else Color(0xFF0F172A)
                                } else {
                                    Color.Gray.copy(alpha = 0.5f)
                                },
                                shape = CircleShape
                            )
                            .clickable { onSelectColor(colorHex) }
                    )
                }
            }

            Box(
                modifier = Modifier
                    .height(28.dp)
                    .width(1.dp)
                    .background(borderColor)
            )

            // Stroke Width Presets
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                StrokeWidthPresets.forEach { (width, label) ->
                    val isSelected = selectedStrokeWidth == width
                    val activeBg = if (isDarkMode) Color(0xFF38BDF8) else Color(0xFF0F172A)
                    val activeFg = if (isDarkMode) Color(0xFF0F172A) else Color(0xFFFFFFFF)
                    val inactiveFg = if (isDarkMode) Color(0xFFCBD5E1) else Color(0xFF475569)

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) activeBg else Color.Transparent)
                            .clickable { onSelectStrokeWidth(width) }
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) activeFg else inactiveFg
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ToolButton(
    label: String,
    isSelected: Boolean,
    isDarkMode: Boolean,
    onClick: () -> Unit
) {
    val activeBg = if (isDarkMode) Color(0xFF38BDF8) else Color(0xFF0F172A)
    val activeFg = if (isDarkMode) Color(0xFF0F172A) else Color(0xFFFFFFFF)
    val inactiveFg = if (isDarkMode) Color(0xFFCBD5E1) else Color(0xFF64748B)

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) activeBg else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) activeFg else inactiveFg
        )
    }
}
