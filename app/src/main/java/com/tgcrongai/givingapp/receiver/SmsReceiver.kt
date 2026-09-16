package com.tgcrongai.givingapp.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.tgcrongai.givingapp.data.AppDatabase
import com.tgcrongai.givingapp.data.TransactionEntity
import com.tgcrongai.givingapp.notification.NotificationManagerHelper
import com.tgcrongai.givingapp.parser.MpesaParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/** Listens for incoming SMS, and — for M-PESA "money received" messages —
 *  stores a pending transaction and raises the purpose-tagging notification. */
class SmsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        val fullBody = messages.joinToString(separator = "") { it.messageBody ?: "" }
        val sender = messages.firstOrNull()?.originatingAddress ?: "MPESA"

        val parsed = MpesaParser.parse(fullBody) ?: return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val dao = AppDatabase.getInstance(context).transactionDao()
                if (dao.getById(parsed.transactionId) == null) {
                    val transaction = TransactionEntity(
                        transactionId = parsed.transactionId,
                        rawSender = sender,
                        fullName = parsed.fullName,
                        firstName = parsed.firstName,
                        phoneNumber = parsed.phoneNumber,
                        amount = parsed.amount,
                        numericAmount = parsed.numericAmount,
                        dateTimeString = parsed.dateTime
                    )
                    dao.insert(transaction)
                    NotificationManagerHelper.showTaggingNotification(context, transaction)

                    // Nudge an immediate sheet sync attempt so dashboards elsewhere stay fresh;
                    // the hourly PeriodicWorkRequest will retry regardless of this call's outcome.
                    val request = OneTimeWorkRequestBuilder<com.tgcrongai.givingapp.work.GoogleSheetsSyncWorker>()
                        .setInputData(workDataOf("trigger" to "new_transaction"))
                        .build()
                    WorkManager.getInstance(context).enqueue(request)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
