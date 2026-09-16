package com.tgcrongai.givingapp.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.tgcrongai.givingapp.data.AppDatabase
import com.tgcrongai.givingapp.data.TxStatus
import com.tgcrongai.givingapp.engine.SmsDispatchEngine
import com.tgcrongai.givingapp.engine.TemplateEngine

/** Renders the active template against a single transaction and sends the SMS. */
class GivingReplyWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    companion object {
        const val KEY_TRANSACTION_ID = "transactionId"
    }

    override suspend fun doWork(): Result {
        val txId = inputData.getString(KEY_TRANSACTION_ID) ?: return Result.failure()
        val db = AppDatabase.getInstance(applicationContext)

        val transaction = db.transactionDao().getById(txId) ?: return Result.failure()
        val template = db.templateDao().getDefault()
            ?: return Result.failure().also {
                db.transactionDao().updateStatus(txId, TxStatus.FAILED, null)
            }

        val message = TemplateEngine.renderGivingTemplate(template.content, transaction)
        val sent = SmsDispatchEngine.sendSms(applicationContext, transaction.phoneNumber, message)

        return if (sent) {
            db.transactionDao().updateStatus(txId, TxStatus.SENT, System.currentTimeMillis())
            Result.success()
        } else {
            db.transactionDao().incrementRetry(txId)
            db.transactionDao().updateStatus(txId, TxStatus.FAILED, null)
            Result.retry()
        }
    }
}
