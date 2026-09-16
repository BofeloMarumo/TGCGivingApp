package com.tgcrongai.givingapp.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tgcrongai.givingapp.data.TransactionEntity
import com.tgcrongai.givingapp.ui.theme.*

private val purposes = listOf("Tithe", "Offering", "Seed")
private val filters = listOf("All", "Pending Tag", "Scheduled", "Sent", "Failed")
private val categories = listOf("All Categories") + purposes

@Composable
fun DashboardScreen(viewModel: DashboardViewModel = viewModel()) {
    val transactions by viewModel.visibleTransactions.collectAsState()
    val filter by viewModel.filter.collectAsState()
    val category by viewModel.category.collectAsState()
    val query by viewModel.query.collectAsState()

    Column(Modifier.fillMaxSize().background(Paper).padding(16.dp)) {

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("THE GO CHURCH · RONGAI", style = MaterialTheme.typography.labelSmall, color = Teal600)
                Text("Giving Queue", style = MaterialTheme.typography.headlineSmall, color = Ink)
            }
            Button(
                onClick = { viewModel.pushAllPending() },
                colors = ButtonDefaults.buttonColors(containerColor = Teal600)
            ) {
                Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Push All")
            }
        }

        Spacer(Modifier.height(12.dp))

        Row(
            Modifier
                .fillMaxWidth()
                .background(Teal50, RoundedCornerShape(12.dp))
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(Modifier.size(7.dp).background(Green, RoundedCornerShape(50)))
            Spacer(Modifier.width(8.dp))
            Text("Sheet synced hourly · pending items in queue", style = MaterialTheme.typography.bodyMedium, color = Teal700)
        }

        Spacer(Modifier.height(10.dp))

        OutlinedTextField(
            value = query,
            onValueChange = viewModel::setQuery,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search name or number") },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            singleLine = true
        )

        Spacer(Modifier.height(10.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items(filters) { f ->
                FilterChip(
                    selected = filter == f,
                    onClick = { viewModel.setFilter(f) },
                    label = { Text(f) },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Teal600, selectedLabelColor = androidx.compose.ui.graphics.Color.White)
                )
            }
        }

        Spacer(Modifier.height(10.dp))

        CategoryDropdown(category = category, onSelected = viewModel::setCategory)

        Spacer(Modifier.height(10.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(transactions, key = { it.transactionId }) { tx ->
                TransactionCard(
                    tx = tx,
                    onTagPurpose = { purpose -> viewModel.tagPurpose(tx.transactionId, purpose) },
                    onSendNow = { viewModel.sendNow(tx.transactionId) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryDropdown(category: String, onSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = category,
            onValueChange = {},
            readOnly = true,
            modifier = Modifier.menuAnchor().fillMaxWidth(),
            trailingIcon = { Icon(Icons.Filled.ArrowDropDown, contentDescription = null) }
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            categories.forEach { c ->
                DropdownMenuItem(text = { Text(c) }, onClick = { onSelected(c); expanded = false })
            }
        }
    }
}

@Composable
private fun statusColors(status: String): Pair<androidx.compose.ui.graphics.Color, androidx.compose.ui.graphics.Color> = when (status) {
    "SENT" -> Green100 to Green
    "FAILED" -> Red100 to Red
    "SCHEDULED" -> Teal100 to Teal700
    else -> Amber100 to Amber
}

@Composable
private fun TransactionCard(tx: TransactionEntity, onTagPurpose: (String) -> Unit, onSendNow: () -> Unit) {
    var purposeMenuOpen by remember { mutableStateOf(false) }
    val (bg, fg) = statusColors(tx.status)

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, LineGray)
    ) {
        Column(Modifier.padding(13.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(tx.fullName, fontWeight = FontWeight.ExtraBold, color = Ink)
                    Text(tx.phoneNumber, style = MaterialTheme.typography.bodyMedium, color = InkSoft)
                }
                Box(
                    Modifier.background(Gold100, RoundedCornerShape(9.dp)).padding(horizontal = 9.dp, vertical = 4.dp)
                ) {
                    Text(tx.amount, fontWeight = FontWeight.ExtraBold, color = Gold)
                }
            }

            Spacer(Modifier.height(6.dp))
            Text(tx.dateTimeString, style = MaterialTheme.typography.labelSmall, color = InkSoft)
            Spacer(Modifier.height(10.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Box {
                    AssistChip(
                        onClick = { purposeMenuOpen = true },
                        label = { Text(tx.purpose) },
                        colors = AssistChipDefaults.assistChipColors(containerColor = Teal50, labelColor = Teal700)
                    )
                    DropdownMenu(expanded = purposeMenuOpen, onDismissRequest = { purposeMenuOpen = false }) {
                        purposes.forEach { p ->
                            DropdownMenuItem(text = { Text(p) }, onClick = { onTagPurpose(p); purposeMenuOpen = false })
                        }
                    }
                }

                Box(Modifier.background(bg, RoundedCornerShape(7.dp)).padding(horizontal = 8.dp, vertical = 4.dp)) {
                    Text(tx.status.replace("_", " "), color = fg, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                }
            }

            if (tx.status == "FAILED" || tx.status == "PENDING_TAG") {
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = onSendNow,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Teal600)
                    ) {
                        Icon(Icons.Filled.Send, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Send Now")
                    }
                    OutlinedButton(onClick = { /* opens an edit-entry dialog in a future pass */ }, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Edit")
                    }
                }
            }
        }
    }
}
