package com.tasknext.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tasknext.app.viewmodel.TaskViewModel

@Composable
fun SettingsScreen(viewModel: TaskViewModel, onBack: () -> Unit, onOpenHistory: () -> Unit = {}) {
    val settings = viewModel.settings
    var notificationsEnabled by remember { mutableStateOf(settings.notificationsEnabled) }
    var defaultHour by remember { mutableStateOf(settings.defaultReminderHour) }
    var defaultMinute by remember { mutableStateOf(settings.defaultReminderMinute) }
    var snoozeMinutes by remember { mutableStateOf(settings.snoozeMinutes) }
    var autoFill by remember { mutableStateOf(settings.autoFillNextTask) }
    var darkTheme by remember { mutableStateOf(settings.darkTheme) }
    var groupByDay by remember { mutableStateOf(settings.groupHistoryByDay) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SettingsSwitchRow("Notifications enabled", notificationsEnabled) {
                notificationsEnabled = it; settings.notificationsEnabled = it
            }

            Text("Default reminder time (used when a task has no custom time)")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = defaultHour.toString(),
                    onValueChange = { v -> v.toIntOrNull()?.coerceIn(0, 23)?.let { defaultHour = it; settings.defaultReminderHour = it } },
                    label = { Text("Hour") }, modifier = Modifier.width(100.dp)
                )
                OutlinedTextField(
                    value = defaultMinute.toString(),
                    onValueChange = { v -> v.toIntOrNull()?.coerceIn(0, 59)?.let { defaultMinute = it; settings.defaultReminderMinute = it } },
                    label = { Text("Minute") }, modifier = Modifier.width(100.dp)
                )
            }

            Text("Snooze duration (minutes)")
            OutlinedTextField(
                value = snoozeMinutes.toString(),
                onValueChange = { v -> v.toIntOrNull()?.let { snoozeMinutes = it; settings.snoozeMinutes = it } },
                modifier = Modifier.width(100.dp)
            )

            SettingsSwitchRow("Auto-fill next task (skip the picker prompt)", autoFill) {
                autoFill = it; settings.autoFillNextTask = it
            }

            SettingsSwitchRow("Dark theme", darkTheme) {
                darkTheme = it; settings.darkTheme = it
            }

            SettingsSwitchRow("Group history by day", groupByDay) {
                groupByDay = it; settings.groupHistoryByDay = it
            }

            Button(onClick = onOpenHistory, modifier = Modifier.fillMaxWidth()) {
                Text("View completed task history")
            }
        }
    }
}

@Composable
private fun SettingsSwitchRow(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onChange)
    }
}
