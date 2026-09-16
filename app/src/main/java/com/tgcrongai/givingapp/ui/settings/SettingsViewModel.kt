package com.tgcrongai.givingapp.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tgcrongai.givingapp.data.SettingsEntity
import com.tgcrongai.givingapp.export.ExportManager
import com.tgcrongai.givingapp.network.HttpUtil
import com.tgcrongai.givingapp.repository.GivingRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed class ConnectionTestState {
    object Idle : ConnectionTestState()
    object Testing : ConnectionTestState()
    data class Result(val success: Boolean) : ConnectionTestState()
}

class SettingsViewModel(app: Application) : AndroidViewModel(app) {

    private val repository = GivingRepository(app)

    val settings: StateFlow<SettingsEntity?> =
        repository.observeSettings().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _testState = MutableStateFlow<ConnectionTestState>(ConnectionTestState.Idle)
    val testState: StateFlow<ConnectionTestState> = _testState

    private val _counts = MutableStateFlow(0 to 0) // total, unsynced
    val counts: StateFlow<Pair<Int, Int>> = _counts

    init {
        viewModelScope.launch {
            _counts.value = repository.totalRecords() to repository.unsyncedCount()
        }
    }

    fun update(newSettings: SettingsEntity) {
        viewModelScope.launch { repository.updateSettings(newSettings) }
    }

    fun testConnection(url: String) {
        if (url.isBlank()) {
            _testState.value = ConnectionTestState.Result(false)
            return
        }
        _testState.value = ConnectionTestState.Testing
        viewModelScope.launch {
            val success = withContext(Dispatchers.IO) { HttpUtil.pingWebhook(url) }
            _testState.value = ConnectionTestState.Result(success)
        }
    }

    fun exportJson() {
        viewModelScope.launch {
            val all = repository.observeTransactions().first()
            ExportManager.exportAndShareJson(getApplication(), all)
        }
    }

    fun exportDocx() {
        viewModelScope.launch {
            val all = repository.observeTransactions().first()
            ExportManager.exportAndShareDocx(getApplication(), all)
        }
    }
}
