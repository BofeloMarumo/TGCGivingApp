package com.tgcrongai.givingapp.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        TransactionEntity::class,
        TemplateEntity::class,
        SettingsEntity::class,
        FollowUpStepEntity::class,
        FollowUpEnrolleeEntity::class,
        FollowUpSentLogEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun transactionDao(): TransactionDao
    abstract fun templateDao(): TemplateDao
    abstract fun settingsDao(): SettingsDao
    abstract fun followUpDao(): FollowUpDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "tgc_giving.db"
                )
                    .addCallback(SeedDataCallback(context))
                    .build().also { INSTANCE = it }
            }
        }
    }

    /** Seeds a sensible default template, default settings row, and a starter
     *  Follow Up sequence the first time the database is created. */
    private class SeedDataCallback(private val context: Context) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            // Room requires suspend DAO access; a lightweight raw-SQL seed keeps
            // this callback synchronous and dependency-free.
            db.execSQL(
                """INSERT INTO templates (title, content, isDefault) VALUES
                   ('Standard Thank You',
                    'Hi <first name>, thank you for your <Purpose> of <Amount> received on <Date>. God bless you richly. - The GO Church, Rongai',
                    1)"""
            )
            db.execSQL(
                "INSERT INTO settings (id, googleSheetWebhookUrl, defaultPurpose, autoSendDelaySeconds, syncInterval) VALUES (1, '', 'Giving', 30, '1hour')"
            )
            db.execSQL("INSERT INTO followup_steps (dayOffset, message) VALUES (1, 'Hi <first name>, it was wonderful having you at The GO Church, Rongai this week. We would love to see you again!')")
            db.execSQL("INSERT INTO followup_steps (dayOffset, message) VALUES (4, '<first name>, just checking in - is there anything we can pray with you about?')")
            db.execSQL("INSERT INTO followup_steps (dayOffset, message) VALUES (10, 'Hi <first name>, our RATC Ministry meets weekly at Rongai Campus. We would love for you to join us. God bless you!')")
        }
    }
}
