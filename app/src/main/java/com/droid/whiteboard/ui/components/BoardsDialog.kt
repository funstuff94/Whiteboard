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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.droid.whiteboard.model.WhiteboardMetadata
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun BoardsDialog(
    boards: List<WhiteboardMetadata>,
    currentBoardId: String,
    isDarkMode: Boolean,
    onSelectBoard: (String) -> Unit,
    onDeleteBoard: (String) -> Unit,
    onNewBoardClick: () -> Unit,
    onDismiss: () -> Unit
) {
    val dialogBg = if (isDarkMode) Color(0xFF1E222A) else Color(0xFFFFFFFF)
    val textPrimary = if (isDarkMode) Color(0xFFF1F5F9) else Color(0xFF0F172A)
    val textSecondary = if (isDarkMode) Color(0xFF94A3B8) else Color(0xFF64748B)

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = dialogBg),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                // Dialog Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Saved Whiteboards",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = textPrimary
                        )
                        Text(
                            text = "${boards.size} whiteboards available",
                            fontSize = 12.sp,
                            color = textSecondary
                        )
                    }

                    Button(
                        onClick = onNewBoardClick,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isDarkMode) Color(0xFF38BDF8) else Color(0xFF0F172A),
                            contentColor = if (isDarkMode) Color(0xFF0F172A) else Color(0xFFFFFFFF)
                        )
                    ) {
                        Text("➕ New", fontWeight = FontWeight.SemiBold)
                    }
                }

                Spacer(Modifier.height(16.dp))

                // List of Whiteboards
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(320.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(boards, key = { it.id }) { meta ->
                        val isCurrent = meta.id == currentBoardId
                        val itemBg = if (isCurrent) {
                            if (isDarkMode) Color(0xFF2C3442) else Color(0xFFE2E8F0)
                        } else {
                            if (isDarkMode) Color(0xFF242A35) else Color(0xFFF8FAFC)
                        }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelectBoard(meta.id) },
                            colors = CardDefaults.cardColors(containerColor = itemBg),
                            shape = RoundedCornerShape(12.dp),
                            border = if (isCurrent) {
                                androidx.compose.foundation.BorderStroke(
                                    2.dp,
                                    if (isDarkMode) Color(0xFF38BDF8) else Color(0xFF0F172A)
                                )
                            } else null
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    // Mode Indicator Icon / Slate
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (meta.isDarkMode) Color(0xFF10141A) else Color(0xFFEDE8D0))
                                            .border(
                                                1.dp,
                                                if (meta.isDarkMode) Color(0xFF475569) else Color(0xFFCBD5E1),
                                                RoundedCornerShape(8.dp)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(text = if (meta.isDarkMode) "🌙" else "☀️", fontSize = 16.sp)
                                    }

                                    Spacer(Modifier.width(12.dp))

                                    Column {
                                        Text(
                                            text = meta.title,
                                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                                            fontSize = 15.sp,
                                            color = textPrimary
                                        )
                                        val dateStr = SimpleDateFormat("MMM d, yyyy • HH:mm", Locale.getDefault())
                                            .format(Date(meta.updatedAt))
                                        Text(
                                            text = "$dateStr • ${meta.strokeCount} strokes",
                                            fontSize = 11.sp,
                                            color = textSecondary
                                        )
                                    }
                                }

                                if (boards.size > 1) {
                                    IconButton(
                                        onClick = { onDeleteBoard(meta.id) }
                                    ) {
                                        Text(text = "❌", fontSize = 14.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Close", color = if (isDarkMode) Color(0xFF38BDF8) else Color(0xFF0F172A))
                    }
                }
            }
        }
    }
}

@Composable
fun NewBoardDialog(
    isDarkModeInitial: Boolean,
    onCreateBoard: (title: String, isDarkMode: Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var isDarkSelected by remember { mutableStateOf(isDarkModeInitial) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create New Whiteboard", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Whiteboard Title") },
                    placeholder = { Text("e.g., Physics Lecture, Project Roadmap") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(16.dp))

                Text("Theme:", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isDarkSelected = true }
                        .padding(vertical = 4.dp)
                ) {
                    RadioButton(
                        selected = isDarkSelected,
                        onClick = { isDarkSelected = true }
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("🌙 Dark Mode (Black board & white marker)")
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isDarkSelected = false }
                        .padding(vertical = 4.dp)
                ) {
                    RadioButton(
                        selected = !isDarkSelected,
                        onClick = { isDarkSelected = false }
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("☀️ Light Mode (White board & dark marker)")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onCreateBoard(title, isDarkSelected) }
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun RenameBoardDialog(
    currentTitle: String,
    onRename: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf(currentTitle) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Rename Whiteboard", fontWeight = FontWeight.Bold) },
        text = {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("New Title") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) onRename(title.trim())
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
