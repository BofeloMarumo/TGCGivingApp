package com.tgcrongai.givingapp.receiver

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.tgcrongai.givingapp.data.AppDatabase
import com.tgcrongai.givingapp.data.TxStatus
import com.tgcrongai.givingapp.notification.NotificationManagerHelper
import com.tgcrongai.givingapp.work.WorkScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/** Fired when the admin taps Tithe / Offering / Seed on the tagging notification. */
class NotificationActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != NotificationManagerHelper.ACTION_TAG_PURPOSE) return

        val txId = intent.getStringExtra(NotificationManagerHelper.EXTRA_TX_ID) ?: return
        val purpose = intent.getStringExtra(NotificationManagerHelper.EXTRA_PURPOSE) ?: return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getInstance(context)
                db.transactionDao().tagPurpose(txId, purpose, TxStatus.SCHEDULED)

                val settings = db.settingsDao().getSettings()
                val delaySeconds = settings?.autoSendDelaySeconds ?: 30
                WorkScheduler.scheduleGivingReply(context, txId, delaySeconds)

                val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                manager.cancel(txId.hashCode())
            } finally {
                pendingResult.finish()
            }
        }
    }
}
