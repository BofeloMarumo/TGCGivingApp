package com.tgcrongai.givingapp.ui.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tgcrongai.givingapp.data.TransactionEntity
import com.tgcrongai.givingapp.repository.GivingRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class DashboardViewModel(app: Application) : AndroidViewModel(app) {

    private val repository = GivingRepository(app)

    private val _filter = MutableStateFlow("All")
    private val _category = MutableStateFlow("All Categories")
    private val _query = MutableStateFlow("")

    val filter: StateFlow<String> = _filter
    val category: StateFlow<String> = _category
    val query: StateFlow<String> = _query

    private val statusForFilter = mapOf(
        "Pending Tag" to "PENDING_TAG",
        "Scheduled" to "SCHEDULED",
        "Sent" to "SENT",
        "Failed" to "FAILED"
    )

    val visibleTransactions: StateFlow<List<TransactionEntity>> =
        combine(repository.observeTransactions(), _filter, _category, _query) { list, filter, category, query ->
            list.filter { tx ->
                val byFilter = filter == "All" || tx.status == statusForFilter[filter]
                val byCategory = category == "All Categories" || tx.purpose == category
                val byQuery = query.isBlank() ||
                    tx.fullName.contains(query, ignoreCase = true) ||
                    tx.phoneNumber.contains(query)
                byFilter && byCategory && byQuery
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setFilter(value: String) { _filter.value = value }
    fun setCategory(value: String) { _category.value = value }
    fun setQuery(value: String) { _query.value = value }

    fun tagPurpose(transactionId: String, purpose: String) {
        viewModelScope.launch { repository.tagPurpose(transactionId, purpose) }
    }

    fun sendNow(transactionId: String) = repository.sendNow(transactionId)

    fun pushAllPending() = repository.pushAllPending()
}
