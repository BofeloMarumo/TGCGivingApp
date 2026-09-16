package com.tgcrongai.givingapp.export

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.tgcrongai.givingapp.data.TransactionEntity
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

object ExportManager {

    fun exportAndShareJson(context: Context, transactions: List<TransactionEntity>) {
        val array = JSONArray()
        transactions.forEach { tx ->
            array.put(
                JSONObject().apply {
                    put("transactionId", tx.transactionId)
                    put("fullName", tx.fullName)
                    put("phoneNumber", tx.phoneNumber)
                    put("amount", tx.amount)
                    put("purpose", tx.purpose)
                    put("dateTime", tx.dateTimeString)
                    put("status", tx.status)
                }
            )
        }
        val file = File(context.cacheDir, "TGC_Giving_Log_${System.currentTimeMillis()}.json")
        file.writeText(array.toString(2))
        shareFile(context, file, "application/json")
    }

    fun exportAndShareDocx(context: Context, transactions: List<TransactionEntity>) {
        val file = File(context.cacheDir, "TGC_Giving_Log_${System.currentTimeMillis()}.docx")
        MinimalDocxWriter.write(
            file = file,
            title = "TGC Rongai - Giving Logs",
            headers = listOf("Date/Time", "Name", "Amount", "Purpose", "Ref Code"),
            rows = transactions.map { listOf(it.dateTimeString, it.fullName, it.amount, it.purpose, it.transactionId) }
        )
        shareFile(context, file, "application/vnd.openxmlformats-officedocument.wordprocessingml.document")
    }

    private fun shareFile(context: Context, file: File, mimeType: String) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            setPackage("com.whatsapp")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        try {
            context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        } catch (e: Exception) {
            val chooser = Intent.createChooser(intent, "Share Giving Log via").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        }
    }
}
