package com.tgcrongai.givingapp.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tgcrongai.givingapp.ui.theme.*

private val delayPresets = listOf("Instant" to 0, "30 sec" to 30, "2 min" to 120, "10 min" to 600)

@Composable
fun SettingsScreen(viewModel: SettingsViewModel = viewModel()) {
    val settings by viewModel.settings.collectAsState()
    val testState by viewModel.testState.collectAsState()
    val counts by viewModel.counts.collectAsState()

    val current = settings ?: return

    var webhook by remember(current) { mutableStateOf(current.googleSheetWebhookUrl) }
    var sendDelay by remember(current) { mutableStateOf(current.autoSendDelaySeconds) }
    var syncInterval by remember(current) { mutableStateOf(current.syncInterval) }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(Paper).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("CONFIGURATION", style = MaterialTheme.typography.labelSmall, color = Teal600)
            Text("Settings & Sync", style = MaterialTheme.typography.headlineSmall, color = Ink)
        }

        item {
            SectionCard(title = "Message Timing") {
                Text(
                    "How long to wait after a purpose is tagged before the giving reply is sent",
                    color = InkSoft, style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(delayPresets) { (label, value) ->
                        FilterChip(
                            selected = sendDelay == value,
                            onClick = { sendDelay = value; viewModel.update(current.copy(autoSendDelaySeconds = value)) },
                            label = { Text(label) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Teal600, selectedLabelColor = androidx.compose.ui.graphics.Color.White)
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = sendDelay.toString(),
                    onValueChange = { input ->
                        val n = input.filter { it.isDigit() }.toIntOrNull() ?: 0
                        sendDelay = n
                        viewModel.update(current.copy(autoSendDelaySeconds = n))
                    },
                    label = { Text("Custom delay (seconds)") },
                    singleLine = true
                )
            }
        }

        item {
            SectionCard(title = "Google Sheet") {
                OutlinedTextField(
                    value = webhook,
                    onValueChange = { webhook = it },
                    label = { Text("Webhook API URL") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(Modifier.height(4.dp))
                LaunchedEffect(webhook) {
                    // Persist the URL as the admin types; debounce is intentionally
                    // skipped here for simplicity but recommended for production.
                    kotlinx.coroutines.delay(500)
                    if (webhook != current.googleSheetWebhookUrl) {
                        viewModel.update(current.copy(googleSheetWebhookUrl = webhook))
                    }
                }
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = { viewModel.testConnection(webhook) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Teal600)
                ) {
                    Icon(Icons.Filled.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        when (testState) {
                            is ConnectionTestState.Testing -> "Testing…"
                            is ConnectionTestState.Result -> if ((testState as ConnectionTestState.Result).success) "Connected ✓" else "Failed — check URL"
                            else -> "Test Connection"
                        }
                    )
                }
            }
        }

        item {
            SectionCard(title = "Sync Interval") {
                listOf("1hour" to "Every 1 hour (automatic)", "manual" to "Manual only").forEach { (id, label) ->
                    Row(
                        Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = syncInterval == id,
                            onClick = { syncInterval = id; viewModel.update(current.copy(syncInterval = id)) },
                            colors = RadioButtonDefaults.colors(selectedColor = Teal600)
                        )
                        Text(label, color = Ink)
                    }
                }
            }
        }

        item {
            SectionCard(title = "Backup & Export") {
                ExportRow(icon = Icons.Filled.Description, label = "Export as JSON & Share to WhatsApp", onClick = viewModel::exportJson)
                Spacer(Modifier.height(8.dp))
                ExportRow(icon = Icons.Filled.Download, label = "Export as DOCX & Share to WhatsApp", onClick = viewModel::exportDocx)
            }
        }

        item {
            Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Teal900)) {
                Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Counter(label = "Total Records", value = counts.first.toString())
                    Counter(label = "Unsynced", value = counts.second.toString())
                }
            }
        }
    }
}

@Composable
private fun SectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(shape = RoundedCornerShape(16.dp), border = androidx.compose.foundation.BorderStroke(1.dp, LineGray)) {
        Column(Modifier.padding(14.dp)) {
            Text(title, fontWeight = FontWeight.ExtraBold, color = Ink)
            Spacer(Modifier.height(10.dp))
            content()
        }
    }
}

@Composable
private fun ExportRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(Teal50, RoundedCornerShape(10.dp))
            .padding(12.dp)
            .clickableRow(onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = Teal700, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(8.dp))
        Text(label, color = Teal700, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
    }
}

private fun Modifier.clickableRow(onClick: () -> Unit): Modifier = this.then(
    androidx.compose.foundation.clickable(onClick = onClick)
)

@Composable
private fun Counter(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = androidx.compose.ui.graphics.Color.White, fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(2.dp))
        Text(label, color = Teal100, style = MaterialTheme.typography.labelSmall)
    }
}
