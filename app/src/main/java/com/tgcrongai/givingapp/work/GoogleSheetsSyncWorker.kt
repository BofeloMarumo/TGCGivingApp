package com.tgcrongai.givingapp.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.tgcrongai.givingapp.data.AppDatabase
import com.tgcrongai.givingapp.network.HttpUtil
import org.json.JSONArray
import org.json.JSONObject

/** Pushes every unsynced giving transaction to the Google Sheet webhook, hourly
 *  (or on demand via "Push All"). Runs again automatically on failure via WorkManager retry. */
class GoogleSheetsSyncWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val db = AppDatabase.getInstance(applicationContext)
        val unsyncedList = db.transactionDao().getUnsyncedTransactions()
        val settings = db.settingsDao().getSettings()

        if (unsyncedList.isEmpty() || settings?.googleSheetWebhookUrl.isNullOrBlank()) {
            return Result.success()
        }

        val payload = JSONArray()
        unsyncedList.forEach { tx ->
            payload.put(
                JSONObject().apply {
                    put("type", "giving")
                    put("transactionId", tx.transactionId)
                    put("fullName", tx.fullName)
                    put("phoneNumber", tx.phoneNumber)
                    put("amount", tx.amount)
                    put("purpose", tx.purpose)
                    put("dateTime", tx.dateTimeString)
                    put("status", tx.status)
                }
            )
        }

        return try {
            val success = HttpUtil.postJson(settings!!.googleSheetWebhookUrl, payload.toString())
            if (success) {
                unsyncedList.forEach { tx -> db.transactionDao().markAsSynced(tx.transactionId) }
                Result.success()
            } else {
                Result.retry()
            }
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
