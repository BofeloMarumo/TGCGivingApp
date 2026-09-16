package com.tgcrongai.givingapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/** A person enrolled into the Follow Up sequence. */
@Entity(tableName = "followup_enrollees")
data class FollowUpEnrolleeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val phoneNumber: String,
    val enrolledTimestamp: Long = System.currentTimeMillis(),
    val status: String = FollowUpEnrolleeStatus.ACTIVE
)

object FollowUpEnrolleeStatus {
    const val ACTIVE = "ACTIVE"
    const val COMPLETED = "COMPLETED"
    const val PAUSED = "PAUSED"
}
