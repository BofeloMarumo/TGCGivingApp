package com.tgcrongai.givingapp.ui.templates

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tgcrongai.givingapp.data.TemplateEntity
import com.tgcrongai.givingapp.repository.GivingRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class TemplatesViewModel(app: Application) : AndroidViewModel(app) {

    private val repository = GivingRepository(app)

    val templates: StateFlow<List<TemplateEntity>> =
        repository.observeTemplates().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _activeId = MutableStateFlow<Long?>(null)
    val activeId: StateFlow<Long?> = _activeId

    fun selectTemplate(id: Long) { _activeId.value = id }

    fun activeOrFirst(list: List<TemplateEntity>): TemplateEntity? =
        list.find { it.id == _activeId.value } ?: list.firstOrNull()

    fun save(template: TemplateEntity) {
        viewModelScope.launch { repository.saveTemplate(template) }
    }
}
