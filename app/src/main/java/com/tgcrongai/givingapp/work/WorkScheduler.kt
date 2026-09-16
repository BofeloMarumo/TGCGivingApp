package com.tgcrongai.givingapp.work

import android.content.Context
import androidx.work.*
import java.util.concurrent.TimeUnit

/** Central place that schedules every background job in the app. */
object WorkScheduler {

    private const val PERIODIC_SYNC_NAME = "google_sheets_periodic_sync"
    private const val PERIODIC_FOLLOWUP_NAME = "followup_periodic_check"

    /** One-time job: send a single giving reply after the configured delay. */
    fun scheduleGivingReply(context: Context, transactionId: String, delaySeconds: Int) {
        val request = OneTimeWorkRequestBuilder<GivingReplyWorker>()
            .setInitialDelay(delaySeconds.toLong(), TimeUnit.SECONDS)
            .setInputData(workDataOf(GivingReplyWorker.KEY_TRANSACTION_ID to transactionId))
            .setBackoffCriteria(BackoffPolicy.LINEAR, 30, TimeUnit.SECONDS)
            .build()
        WorkManager.getInstance(context).enqueue(request)
    }

    /** "Send Now" button: same worker, no delay. */
    fun sendGivingReplyImmediately(context: Context, transactionId: String) =
        scheduleGivingReply(context, transactionId, 0)

    /** Hourly (or manual) push of unsynced transactions to the Google Sheet webhook. */
    fun ensurePeriodicSync(context: Context, enabled: Boolean) {
        val workManager = WorkManager.getInstance(context)
        if (!enabled) {
            workManager.cancelUniqueWork(PERIODIC_SYNC_NAME)
            return
        }
        val request = PeriodicWorkRequestBuilder<GoogleSheetsSyncWorker>(1, TimeUnit.HOURS)
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .build()
        workManager.enqueueUniquePeriodicWork(
            PERIODIC_SYNC_NAME, ExistingPeriodicWorkPolicy.KEEP, request
        )
    }

    fun syncNow(context: Context) {
        val request = OneTimeWorkRequestBuilder<GoogleSheetsSyncWorker>().build()
        WorkManager.getInstance(context).enqueue(request)
    }

    /** Checks, every 6 hours, whether any Follow Up enrollee has a message due today. */
    fun ensureFollowUpPeriodicCheck(context: Context) {
        val request = PeriodicWorkRequestBuilder<FollowUpCheckWorker>(6, TimeUnit.HOURS)
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            PERIODIC_FOLLOWUP_NAME, ExistingPeriodicWorkPolicy.KEEP, request
        )
    }

    /** "Run Follow-Up Check Now" button, for testing without waiting for the periodic tick. */
    fun runFollowUpCheckNow(context: Context) {
        val request = OneTimeWorkRequestBuilder<FollowUpCheckWorker>().build()
        WorkManager.getInstance(context).enqueue(request)
    }
}
