package com.tgcrongai.givingapp.parser

/** Parses an incoming M-PESA "money received" SMS body into structured fields. */
object MpesaParser {

    fun parse(messageBody: String): ParsedData? {
        if (!messageBody.contains("Confirmed", ignoreCase = true) ||
            !messageBody.contains("received", ignoreCase = true)
        ) {
            return null
        }

        return try {
            val refRegex = Regex("""^([A-Z0-9]+)\s+Confirmed""", RegexOption.IGNORE_CASE)
            val amountRegex = Regex("""Ksh\s*([\d,]+\.\d{2})""", RegexOption.IGNORE_CASE)
            val nameRegex = Regex("""from\s+([A-Z\s]+)\s+(\d{10}|\d{4}\*{3}\d{3})""", RegexOption.IGNORE_CASE)
            val dateTimeRegex = Regex("""on\s+(\d{1,2}/\d{1,2}/\d{2,4}\s+at\s+\d{1,2}:\d{2}\s+[AP]M)""", RegexOption.IGNORE_CASE)

            val refMatch = refRegex.find(messageBody)?.groupValues?.get(1) ?: return null
            val amountMatch = amountRegex.find(messageBody)?.groupValues?.get(1) ?: "0.00"
            val rawName = nameRegex.find(messageBody)?.groupValues?.get(1)?.trim() ?: "Donor"
            val phoneMatch = nameRegex.find(messageBody)?.groupValues?.get(2) ?: ""
            val dateTimeMatch = dateTimeRegex.find(messageBody)?.groupValues?.get(1) ?: ""

            val formattedFullName = rawName.lowercase()
                .split(" ")
                .filter { it.isNotBlank() }
                .joinToString(" ") { it.replaceFirstChar(Char::uppercase) }
            val firstName = formattedFullName.split(" ").firstOrNull() ?: formattedFullName

            ParsedData(
                transactionId = refMatch,
                fullName = formattedFullName,
                firstName = firstName,
                phoneNumber = phoneMatch,
                amount = "Ksh$amountMatch",
                numericAmount = amountMatch.replace(",", "").toDoubleOrNull() ?: 0.0,
                dateTime = dateTimeMatch
            )
        } catch (e: Exception) {
            null
        }
    }
}

data class ParsedData(
    val transactionId: String,
    val fullName: String,
    val firstName: String,
    val phoneNumber: String,
    val amount: String,
    val numericAmount: Double,
    val dateTime: String
)
