package com.tgcrongai.givingapp

import android.app.Application
import com.tgcrongai.givingapp.notification.NotificationManagerHelper
import com.tgcrongai.givingapp.work.WorkScheduler

class GivingApp : Application() {
    override fun onCreate() {
        super.onCreate()
        NotificationManagerHelper.ensureChannel(this)
        WorkScheduler.ensurePeriodicSync(this, enabled = true)
        WorkScheduler.ensureFollowUpPeriodicCheck(this)
    }
}
