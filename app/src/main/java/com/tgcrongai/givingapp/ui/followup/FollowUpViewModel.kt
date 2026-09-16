package com.tgcrongai.givingapp.ui.followup

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tgcrongai.givingapp.data.FollowUpEnrolleeEntity
import com.tgcrongai.givingapp.data.FollowUpStepEntity
import com.tgcrongai.givingapp.engine.TemplateEngine
import com.tgcrongai.givingapp.repository.FollowUpRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class EnrolleeProgress(
    val enrollee: FollowUpEnrolleeEntity,
    val sentCount: Int,
    val totalSteps: Int,
    val nextStep: FollowUpStepEntity?
)

class FollowUpViewModel(app: Application) : AndroidViewModel(app) {

    private val repository = FollowUpRepository(app)

    init {
        repository.ensurePeriodicCheckScheduled()
    }

    val steps: StateFlow<List<FollowUpStepEntity>> =
        repository.observeSteps().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val enrollees = repository.observeEnrollees()
    private val sentLog = repository.observeSentLog()

    val enrolleeProgress: StateFlow<List<EnrolleeProgress>> =
        combine(enrollees, sentLog, steps) { people, logs, stepList ->
            val sorted = stepList.sortedBy { it.dayOffset }
            people.map { person ->
                val sentIds = logs.filter { it.enrolleeId == person.id }.map { it.stepId }.toSet()
                val next = sorted.firstOrNull { it.id !in sentIds }
                EnrolleeProgress(person, sentIds.size, sorted.size, next)
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addStep(dayOffset: Int, message: String) {
        viewModelScope.launch { repository.addStep(dayOffset, message) }
    }

    fun updateStep(step: FollowUpStepEntity) {
        viewModelScope.launch { repository.updateStep(step) }
    }

    fun removeStep(step: FollowUpStepEntity) {
        viewModelScope.launch { repository.removeStep(step) }
    }

    fun enroll(name: String, phone: String) {
        if (name.isBlank() || phone.isBlank()) return
        viewModelScope.launch { repository.enroll(name.trim(), phone.trim()) }
    }

    fun unenroll(enrollee: FollowUpEnrolleeEntity) {
        viewModelScope.launch { repository.unenroll(enrollee) }
    }

    fun runCheckNow() = repository.runCheckNow()
}
