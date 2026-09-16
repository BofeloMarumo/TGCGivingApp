package com.tgcrongai.givingapp.ui.followup

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tgcrongai.givingapp.data.FollowUpEnrolleeEntity
import com.tgcrongai.givingapp.data.FollowUpStepEntity
import com.tgcrongai.givingapp.data.FollowUpTokens
import com.tgcrongai.givingapp.ui.theme.*

@Composable
fun FollowUpScreen(viewModel: FollowUpViewModel = viewModel()) {
    val steps by viewModel.steps.collectAsState()
    val progress by viewModel.enrolleeProgress.collectAsState()

    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(Paper).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text("SECOND SEQUENCE", style = MaterialTheme.typography.labelSmall, color = Teal600)
            Text("Follow Up", style = MaterialTheme.typography.headlineSmall, color = Ink)
            Spacer(Modifier.height(4.dp))
            Text(
                "A separate text sequence from the Giving Queue — for welcoming visitors, checking in, or nurturing contacts over time.",
                color = InkSoft
            )
        }

        item { SequenceBuilderCard(steps = steps, viewModel = viewModel) }

        item {
            EnrollCard(
                name = name, onNameChange = { name = it },
                phone = phone, onPhoneChange = { phone = it },
                onEnroll = {
                    viewModel.enroll(name, phone)
                    name = ""; phone = ""
                },
                progress = progress,
                onUnenroll = viewModel::unenroll
            )
        }

        item {
            Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Teal900)) {
                Row(
                    Modifier.fillMaxWidth().padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("BACKGROUND CHECK", color = Teal100, style = MaterialTheme.typography.labelSmall)
                        Text("Runs automatically every 6 hours", color = androidx.compose.ui.graphics.Color.White, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = viewModel::runCheckNow,
                        colors = ButtonDefaults.buttonColors(containerColor = androidx.compose.ui.graphics.Color.White, contentColor = Teal900)
                    ) {
                        Icon(Icons.Filled.FastForward, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Run Now")
                    }
                }
            }
        }
    }
}

@Composable
private fun SequenceBuilderCard(steps: List<FollowUpStepEntity>, viewModel: FollowUpViewModel) {
    val sorted = steps.sortedBy { it.dayOffset }

    Card(shape = RoundedCornerShape(16.dp), border = androidx.compose.foundation.BorderStroke(1.dp, LineGray)) {
        Column(Modifier.padding(14.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Sequence · ${steps.size} message${if (steps.size != 1) "s" else ""}", fontWeight = FontWeight.ExtraBold, color = Ink)
                AssistChip(
                    onClick = { viewModel.addStep(dayOffset = (sorted.lastOrNull()?.dayOffset ?: 0) + 3, message = "Hi <first name>, just following up with you!") },
                    label = { Text("Add Message") },
                    leadingIcon = { Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(14.dp)) },
                    colors = AssistChipDefaults.assistChipColors(containerColor = Teal50, labelColor = Teal700)
                )
            }
            Spacer(Modifier.height(10.dp))

            sorted.forEachIndexed { index, step ->
                StepRow(index = index, step = step, canDelete = steps.size > 1, viewModel = viewModel)
                if (index != sorted.lastIndex) Spacer(Modifier.height(10.dp))
            }
        }
    }
}

@Composable
private fun StepRow(index: Int, step: FollowUpStepEntity, canDelete: Boolean, viewModel: FollowUpViewModel) {
    var message by remember(step.id) { mutableStateOf(step.message) }
    var dayText by remember(step.id) { mutableStateOf(step.dayOffset.toString()) }

    Column(
        Modifier
            .fillMaxWidth()
            .background(androidx.compose.ui.graphics.Color.White, RoundedCornerShape(12.dp))
    ) {
        Column(Modifier.padding(11.dp).background(androidx.compose.ui.graphics.Color.Transparent)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier.size(20.dp).background(Teal600, RoundedCornerShape(50)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("${index + 1}", color = androidx.compose.ui.graphics.Color.White, style = MaterialTheme.typography.labelSmall)
                    }
                    Spacer(Modifier.width(7.dp))
                    Text("Send", color = InkSoft, style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.width(6.dp))
                    OutlinedTextField(
                        value = dayText,
                        onValueChange = { input ->
                            dayText = input.filter { it.isDigit() }
                            dayText.toIntOrNull()?.let { viewModel.updateStep(step.copy(dayOffset = it)) }
                        },
                        modifier = Modifier.width(64.dp),
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(Modifier.width(6.dp))
                    Text("day(s) after enrollment", color = InkSoft, style = MaterialTheme.typography.bodyMedium)
                }
                if (canDelete) {
                    IconButton(onClick = { viewModel.removeStep(step) }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Remove step", tint = Red)
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = message,
                onValueChange = {
                    message = it
                    viewModel.updateStep(step.copy(message = it))
                },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                FollowUpTokens.ALL.forEach { token ->
                    AssistChip(
                        onClick = {
                            val newMessage = message + token
                            message = newMessage
                            viewModel.updateStep(step.copy(message = newMessage))
                        },
                        label = { Text("+ $token") },
                        colors = AssistChipDefaults.assistChipColors(containerColor = Teal50, labelColor = Teal700)
                    )
                }
            }
        }
    }
}

@Composable
private fun EnrollCard(
    name: String, onNameChange: (String) -> Unit,
    phone: String, onPhoneChange: (String) -> Unit,
    onEnroll: () -> Unit,
    progress: List<EnrolleeProgress>,
    onUnenroll: (FollowUpEnrolleeEntity) -> Unit
) {
    Card(shape = RoundedCornerShape(16.dp), border = androidx.compose.foundation.BorderStroke(1.dp, LineGray)) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.PersonAdd, contentDescription = null, tint = Ink, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Enroll a contact", fontWeight = FontWeight.ExtraBold, color = Ink)
            }
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(value = name, onValueChange = onNameChange, label = { Text("Full name") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(value = phone, onValueChange = onPhoneChange, label = { Text("Mobile number") }, modifier = Modifier.weight(1f), singleLine = true)
                Spacer(Modifier.width(8.dp))
                Button(onClick = onEnroll, colors = ButtonDefaults.buttonColors(containerColor = Gold)) { Text("Enroll") }
            }

            Spacer(Modifier.height(14.dp))
            Text("${progress.size} enrolled", color = InkSoft, style = MaterialTheme.typography.labelSmall)
            Spacer(Modifier.height(8.dp))

            if (progress.isEmpty()) {
                Text("No one enrolled yet.", color = InkSoft, modifier = Modifier.fillMaxWidth())
            }
            progress.forEach { p ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .background(androidx.compose.ui.graphics.Color.White, RoundedCornerShape(11.dp))
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(p.enrollee.name, fontWeight = FontWeight.ExtraBold, color = Ink)
                        Text(p.enrollee.phoneNumber, color = InkSoft, style = MaterialTheme.typography.bodyMedium)
                        val nextText = p.nextStep?.let { "next: message on day ${it.dayOffset}" } ?: "sequence complete"
                        Text("${p.sentCount}/${p.totalSteps} sent · $nextText", color = Teal700, style = MaterialTheme.typography.labelSmall)
                    }
                    IconButton(onClick = { onUnenroll(p.enrollee) }) {
                        Icon(Icons.Filled.Close, contentDescription = "Remove", tint = InkSoft)
                    }
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}
