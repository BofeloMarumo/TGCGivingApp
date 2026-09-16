package com.tgcrongai.givingapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * One row per intercepted M-PESA "money received" SMS.
 * Lifecycle: PENDING_TAG -> SCHEDULED -> SENT (or FAILED, which can be retried).
 */
@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey val transactionId: String,   // e.g. "UIE3K79O1C"
    val rawSender: String,                   // e.g. "MPESA"
    val fullName: String,                    // e.g. "Beverly Kituzi"
    val firstName: String,                   // e.g. "Beverly"
    val phoneNumber: String,                 // e.g. "0702***566"
    val amount: String,                      // e.g. "Ksh99.00"
    val numericAmount: Double,               // e.g. 99.00
    val dateTimeString: String,              // e.g. "14/9/26 at 2:59 PM"
    var purpose: String = "Unassigned",      // "Tithe", "Offering", "Seed", or custom
    var status: String = "PENDING_TAG",      // PENDING_TAG, SCHEDULED, SENT, FAILED
    var retryCount: Int = 0,
    val receivedTimestamp: Long = System.currentTimeMillis(),
    var sentTimestamp: Long? = null,
    var syncedOnline: Boolean = false
)

object TxStatus {
    const val PENDING_TAG = "PENDING_TAG"
    const val SCHEDULED = "SCHEDULED"
    const val SENT = "SENT"
    const val FAILED = "FAILED"
}
