package com.tgcrongai.givingapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Records that a given step has already gone out to a given enrollee, so the
 * daily check never sends the same message twice. One row per (enrollee, step).
 */
@Entity(tableName = "followup_sent_log")
data class FollowUpSentLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val enrolleeId: Long,
    val stepId: Long,
    val sentTimestamp: Long = System.currentTimeMillis(),
    val success: Boolean = true
)
