package com.tgcrongai.givingapp.ui.templates

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tgcrongai.givingapp.data.GivingTokens
import com.tgcrongai.givingapp.data.TemplateEntity
import com.tgcrongai.givingapp.engine.TemplateEngine
import com.tgcrongai.givingapp.ui.theme.*

private data class SampleTx(
    val firstName: String = "Beverly",
    val fullName: String = "Beverly Kituzi",
    val amount: String = "Ksh99.00",
    val purpose: String = "Tithe",
    val dateTime: String = "14/9/26 at 2:59 PM"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemplatesScreen(viewModel: TemplatesViewModel = viewModel()) {
    val templates by viewModel.templates.collectAsState()
    val active = viewModel.activeOrFirst(templates)

    var fieldValue by remember(active?.id) {
        mutableStateOf(TextFieldValue(active?.content.orEmpty()))
    }
    var dropdownOpen by remember { mutableStateOf(false) }
    val sample = remember { SampleTx() }

    Column(Modifier.fillMaxSize().background(Paper).padding(16.dp)) {
        Text("MESSAGE TEMPLATES", style = MaterialTheme.typography.labelSmall, color = Teal600)
        Text("Template Manager", style = MaterialTheme.typography.headlineSmall, color = Ink)
        Spacer(Modifier.height(14.dp))

        if (active == null) {
            Text("No templates yet.", color = InkSoft)
            return@Column
        }

        Text("Active template", style = MaterialTheme.typography.labelSmall, color = InkSoft)
        Spacer(Modifier.height(6.dp))
        ExposedDropdownMenuBox(expanded = dropdownOpen, onExpandedChange = { dropdownOpen = it }) {
            OutlinedTextField(
                value = active.title + if (active.isDefault) " (default)" else "",
                onValueChange = {},
                readOnly = true,
                modifier = Modifier.menuAnchor().fillMaxWidth(),
                trailingIcon = { Icon(Icons.Filled.ArrowDropDown, contentDescription = null) }
            )
            ExposedDropdownMenu(expanded = dropdownOpen, onDismissRequest = { dropdownOpen = false }) {
                templates.forEach { t ->
                    DropdownMenuItem(
                        text = { Text(t.title + if (t.isDefault) " (default)" else "") },
                        onClick = { viewModel.selectTemplate(t.id); dropdownOpen = false }
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        Card(shape = RoundedCornerShape(16.dp), border = androidx.compose.foundation.BorderStroke(1.dp, LineGray)) {
            Column(Modifier.padding(14.dp)) {
                Text("Editor", fontWeight = FontWeight.ExtraBold, color = Ink)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = fieldValue,
                    onValueChange = { fieldValue = it },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 4
                )
                Spacer(Modifier.height(10.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(GivingTokens.ALL) { token ->
                        AssistChip(
                            onClick = {
                                val cursor = fieldValue.selection.start.takeIf { it >= 0 } ?: fieldValue.text.length
                                val newText = fieldValue.text.substring(0, cursor) + token + fieldValue.text.substring(cursor)
                                fieldValue = TextFieldValue(newText, selection = TextRange(cursor + token.length))
                            },
                            label = { Text("+ $token") },
                            colors = AssistChipDefaults.assistChipColors(containerColor = Teal50, labelColor = Teal700)
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(14.dp))

        Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Teal900)) {
            Column(Modifier.padding(14.dp)) {
                Text("LIVE PREVIEW · sample data", color = Teal100, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                Spacer(Modifier.height(8.dp))
                Box(Modifier.background(Color.White.copy(alpha = 0.08f), RoundedCornerShape(12.dp)).padding(12.dp)) {
                    val rendered = TemplateEngine.render(
                        fieldValue.text,
                        mapOf(
                            "<first name>" to sample.firstName,
                            "<full name>" to sample.fullName,
                            "<Amount>" to sample.amount,
                            "<Purpose>" to sample.purpose,
                            "<Date>" to sample.dateTime
                        )
                    )
                    Text(rendered, color = Color.White)
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = { viewModel.save(active.copy(content = fieldValue.text)) },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Gold)
        ) {
            Text("Save Template", fontWeight = FontWeight.ExtraBold)
        }
    }
}
