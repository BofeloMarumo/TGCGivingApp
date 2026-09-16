package com.tgcrongai.givingapp.engine

import android.content.Context
import android.telephony.SmsManager

/** Thin wrapper around SmsManager, shared by the giving-reply flow and the
 *  follow-up flow. Splits long messages automatically. */
object SmsDispatchEngine {

    fun sendSms(context: Context, phoneNumber: String, message: String): Boolean {
        return try {
            val smsManager: SmsManager =
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                    context.getSystemService(SmsManager::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    SmsManager.getDefault()
                }
            val parts = smsManager.divideMessage(message)
            smsManager.sendMultipartTextMessage(phoneNumber, null, parts, null, null)
            true
        } catch (e: Exception) {
            false
        }
    }
}
