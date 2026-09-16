package com.tgcrongai.givingapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Single-row configuration table. */
@Entity(tableName = "settings")
data class SettingsEntity(
    @PrimaryKey val id: Int = 1,
    val googleSheetWebhookUrl: String = "",
    val defaultPurpose: String = "Giving",
    /** How long to wait after a purpose is tagged before the reply SMS actually goes out. */
    val autoSendDelaySeconds: Int = 30,
    /** "1hour" (WorkManager periodic sync) or "manual" (user must tap Push All). */
    val syncInterval: String = "1hour"
)
