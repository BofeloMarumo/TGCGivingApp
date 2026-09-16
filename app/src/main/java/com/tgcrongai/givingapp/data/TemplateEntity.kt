package com.tgcrongai.givingapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/** A reusable giving-acknowledgement message with replacement tokens. */
@Entity(tableName = "templates")
data class TemplateEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val content: String,
    val isDefault: Boolean = false
)

/** Tokens supported inside a giving-reply template. */
object GivingTokens {
    const val FIRST_NAME = "<first name>"
    const val FULL_NAME = "<full name>"
    const val AMOUNT = "<Amount>"
    const val PURPOSE = "<Purpose>"
    const val DATE = "<Date>"
    val ALL = listOf(FIRST_NAME, FULL_NAME, AMOUNT, PURPOSE, DATE)
}
