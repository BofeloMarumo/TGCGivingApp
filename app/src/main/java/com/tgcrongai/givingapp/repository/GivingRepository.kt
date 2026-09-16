package com.tgcrongai.givingapp.repository

import android.content.Context
import com.tgcrongai.givingapp.data.*
import com.tgcrongai.givingapp.work.WorkScheduler
import kotlinx.coroutines.flow.Flow

/** Single entry point the Dashboard / Templates / Settings ViewModels talk to. */
class GivingRepository(private val context: Context) {

    private val db = AppDatabase.getInstance(context)

    fun observeTransactions(): Flow<List<TransactionEntity>> = db.transactionDao().observeAll()
    fun observeTemplates(): Flow<List<TemplateEntity>> = db.templateDao().observeAll()
    fun observeSettings(): Flow<SettingsEntity?> = db.settingsDao().observe()

    suspend fun tagPurpose(transactionId: String, purpose: String) {
        db.transactionDao().tagPurpose(transactionId, purpose, TxStatus.SCHEDULED)
        val delay = db.settingsDao().getSettings()?.autoSendDelaySeconds ?: 30
        WorkScheduler.scheduleGivingReply(context, transactionId, delay)
    }

    fun sendNow(transactionId: String) {
        WorkScheduler.sendGivingReplyImmediately(context, transactionId)
    }

    fun pushAllPending() {
        WorkScheduler.syncNow(context)
    }

    suspend fun saveTemplate(template: TemplateEntity) {
        if (template.id == 0L) db.templateDao().insert(template) else db.templateDao().update(template)
    }

    suspend fun updateSettings(settings: SettingsEntity) {
        db.settingsDao().upsert(settings)
        WorkScheduler.ensurePeriodicSync(context, settings.syncInterval == "1hour")
    }

    suspend fun getAllTransactionsOnce() = db.transactionDao().observeAll()
    suspend fun totalRecords() = db.transactionDao().getTotalCount()
    suspend fun unsyncedCount() = db.transactionDao().getUnsyncedCount()
}
