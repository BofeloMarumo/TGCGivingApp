package com.tgcrongai.givingapp.engine

import com.tgcrongai.givingapp.data.FollowUpEnrolleeEntity
import com.tgcrongai.givingapp.data.TransactionEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Fills a template's token placeholders in with real values. Shared by both
 *  the Giving Queue templates and the Follow Up sequence templates. */
object TemplateEngine {

    fun render(content: String, tokenValues: Map<String, String>): String {
        var result = content
        tokenValues.forEach { (token, value) -> result = result.replace(token, value) }
        return result
    }

    fun renderGivingTemplate(content: String, transaction: TransactionEntity): String = render(
        content,
        mapOf(
            "<first name>" to transaction.firstName,
            "<full name>" to transaction.fullName,
            "<Amount>" to transaction.amount,
            "<Purpose>" to transaction.purpose,
            "<Date>" to transaction.dateTimeString
        )
    )

    fun renderFollowUpTemplate(content: String, enrollee: FollowUpEnrolleeEntity): String = render(
        content,
        mapOf(
            "<first name>" to enrollee.name.trim().split(" ").firstOrNull().orEmpty(),
            "<full name>" to enrollee.name
        )
    )

    private val dayFormat = SimpleDateFormat("d/M/yy", Locale.getDefault())

    /** Whole days elapsed between an enrollment timestamp and now (used by the
     *  Follow Up daily check to decide which step, if any, is due). */
    fun daysSince(timestampMillis: Long, nowMillis: Long = System.currentTimeMillis()): Int {
        val diff = nowMillis - timestampMillis
        return (diff / (1000L * 60 * 60 * 24)).toInt()
    }
}
