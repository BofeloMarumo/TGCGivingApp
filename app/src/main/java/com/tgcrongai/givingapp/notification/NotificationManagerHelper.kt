package com.tgcrongai.givingapp.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.tgcrongai.givingapp.R
import com.tgcrongai.givingapp.data.TransactionEntity
import com.tgcrongai.givingapp.receiver.NotificationActionReceiver

object NotificationManagerHelper {
    const val CHANNEL_ID = "giving_tagging_channel"
    const val ACTION_TAG_PURPOSE = "ACTION_TAG_PURPOSE"
    const val EXTRA_TX_ID = "TX_ID"
    const val EXTRA_PURPOSE = "PURPOSE"

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, "New Giving Alerts", NotificationManager.IMPORTANCE_HIGH
            ).apply { description = "Tag the purpose of a new M-PESA giving SMS" }
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    fun showTaggingNotification(context: Context, transaction: TransactionEntity) {
        ensureChannel(context)

        fun actionPendingIntent(requestCode: Int, purpose: String): PendingIntent {
            val intent = Intent(context, NotificationActionReceiver::class.java).apply {
                action = ACTION_TAG_PURPOSE
                putExtra(EXTRA_TX_ID, transaction.transactionId)
                putExtra(EXTRA_PURPOSE, purpose)
            }
            return PendingIntent.getBroadcast(
                context, requestCode, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("New Giving: ${transaction.amount} from ${transaction.firstName}")
            .setContentText("Select Giving Purpose to dispatch reply:")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .addAction(0, "Tithe", actionPendingIntent(1, "Tithe"))
            .addAction(0, "Offering", actionPendingIntent(2, "Offering"))
            .addAction(0, "Seed", actionPendingIntent(3, "Seed"))
            .setAutoCancel(true)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(transaction.transactionId.hashCode(), notification)
    }
}
