package com.tasknext.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.tasknext.app.data.Priority
import com.tasknext.app.data.Task

private val palette = listOf(
    "#4F46E5", "#22C55E", "#EF4444", "#F59E0B", "#0EA5E9", "#EC4899", "#6B7280"
)

@Composable
fun AddEditTaskDialog(
    existing: Task?,
    onDismiss: () -> Unit,
    onSave: (title: String, color: String, priority: Priority, comment: String, reminderOn: Boolean, hour: Int?, minute: Int?) -> Unit
) {
    var title by remember { mutableStateOf(existing?.title ?: "") }
    var color by remember { mutableStateOf(existing?.colorHex ?: palette.first()) }
    var priority by remember { mutableStateOf(existing?.priority ?: Priority.MEDIUM) }
    var comment by remember { mutableStateOf(existing?.comment ?: "") }
    var reminderOn by remember { mutableStateOf(existing?.reminderEnabled ?: false) }
    var hour by remember { mutableStateOf(existing?.reminderHour ?: 9) }
    var minute by remember { mutableStateOf(existing?.reminderMinute ?: 0) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existing == null) "Add task" else "Edit task") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title, onValueChange = { title = it },
                    label = { Text("Title") }, singleLine = true
                )
                Text("Color")
                var colorExpanded by remember { mutableStateOf(false) }
                Box {
                    val swatch = Color(android.graphics.Color.parseColor(color))
                    OutlinedButton(onClick = { colorExpanded = true }) {
                        Box(Modifier.size(16.dp).background(swatch, CircleShape))
                        Spacer(Modifier.width(8.dp))
                        Text(color)
                    }
                    DropdownMenu(expanded = colorExpanded, onDismissRequest = { colorExpanded = false }) {
                        palette.forEach { hex ->
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(Modifier.size(16.dp).background(Color(android.graphics.Color.parseColor(hex)), CircleShape))
                                        Spacer(Modifier.width(8.dp))
                                        Text(hex)
                                    }
                                },
                                onClick = { color = hex; colorExpanded = false }
                            )
                        }
                    }
                }

                Text("Priority")
                Row {
                    Priority.entries.forEach { p ->
                        FilterChip(
                            selected = priority == p,
                            onClick = { priority = p },
                            label = { Text(p.name) },
                            modifier = Modifier.padding(end = 6.dp)
                        )
                    }
                }

                OutlinedTextField(
                    value = comment, onValueChange = { comment = it },
                    label = { Text("Comment (optional)") }
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = reminderOn, onCheckedChange = { reminderOn = it })
                    Text("Remind me")
                }
                if (reminderOn) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = hour.toString(),
                            onValueChange = { hour = it.toIntOrNull()?.coerceIn(0, 23) ?: hour },
                            label = { Text("Hour") },
                            modifier = Modifier.width(90.dp)
                        )
                        OutlinedTextField(
                            value = minute.toString(),
                            onValueChange = { minute = it.toIntOrNull()?.coerceIn(0, 59) ?: minute },
                            label = { Text("Minute") },
                            modifier = Modifier.width(90.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = title.isNotBlank(),
                onClick = {
                    onSave(title.trim(), color, priority, comment.trim(), reminderOn, if (reminderOn) hour else null, if (reminderOn) minute else null)
                }
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
