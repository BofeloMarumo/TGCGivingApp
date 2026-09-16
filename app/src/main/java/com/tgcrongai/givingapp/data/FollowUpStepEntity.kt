package com.tgcrongai.givingapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * One message in the "Follow Up" sequence — a drip campaign that is entirely
 * separate from the everyday Giving Queue. dayOffset is measured in whole days
 * after a contact's enrollment date.
 */
@Entity(tableName = "followup_steps")
data class FollowUpStepEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dayOffset: Int,     // "send this many days after enrollment"
    val message: String     // supports <first name> / <full name> tokens
)

/** Tokens supported inside a follow-up step message. */
object FollowUpTokens {
    const val FIRST_NAME = "<first name>"
    const val FULL_NAME = "<full name>"
    val ALL = listOf(FIRST_NAME, FULL_NAME)
}
