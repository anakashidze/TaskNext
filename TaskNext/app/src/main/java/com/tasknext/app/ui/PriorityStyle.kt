package com.tasknext.app.ui

import androidx.compose.ui.graphics.Color
import com.tasknext.app.data.Priority

internal data class PriorityPalette(
    val container: Color,
    val content: Color
)

internal fun priorityPalette(priority: Priority): PriorityPalette = when (priority) {
    Priority.LOW -> PriorityPalette(
        container = Color(0xFFE0F2FE),
        content = Color(0xFF0369A1)
    )
    Priority.MEDIUM -> PriorityPalette(
        container = Color(0xFFEDE9FE),
        content = Color(0xFF6D28D9)
    )
    Priority.HIGH -> PriorityPalette(
        container = Color(0xFFFFE4E6),
        content = Color(0xFFBE123C)
    )
}
