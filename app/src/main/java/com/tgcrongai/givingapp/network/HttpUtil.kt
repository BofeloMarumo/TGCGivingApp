package com.tgcrongai.givingapp.network

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

object HttpUtil {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private val JSON = "application/json; charset=utf-8".toMediaType()

    fun postJson(url: String, json: String): Boolean {
        val body = json.toRequestBody(JSON)
        val request = Request.Builder().url(url).post(body).build()
        client.newCall(request).execute().use { response ->
            return response.isSuccessful
        }
    }

    /** Used by the Settings screen's "Test Connection" button. */
    fun pingWebhook(url: String): Boolean {
        return try {
            postJson(url, "[]")
        } catch (e: Exception) {
            false
        }
    }
}
