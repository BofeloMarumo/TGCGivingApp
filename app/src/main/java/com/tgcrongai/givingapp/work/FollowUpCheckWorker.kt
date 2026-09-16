package com.tgcrongai.givingapp.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.tgcrongai.givingapp.data.AppDatabase
import com.tgcrongai.givingapp.data.FollowUpSentLogEntity
import com.tgcrongai.givingapp.engine.SmsDispatchEngine
import com.tgcrongai.givingapp.engine.TemplateEngine

/**
 * Runs periodically (see WorkScheduler.ensureFollowUpPeriodicCheck) and once
 * manually via the "Run Follow-Up Check Now" button. For every active
 * enrollee, sends any step whose dayOffset has been reached and that hasn't
 * already gone out, then logs it so it is never sent twice.
 */
class FollowUpCheckWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val db = AppDatabase.getInstance(applicationContext)
        val dao = db.followUpDao()

        val steps = dao.getStepsOnce()
        if (steps.isEmpty()) return Result.success()

        val enrollees = dao.getActiveEnrolleesOnce()
        val now = System.currentTimeMillis()

        for (enrollee in enrollees) {
            val daysIn = TemplateEngine.daysSince(enrollee.enrolledTimestamp, now)
            val alreadySent = dao.getSentStepIds(enrollee.id).toSet()

            val due = steps.filter { it.dayOffset <= daysIn && it.id !in alreadySent }
            for (step in due) {
                val message = TemplateEngine.renderFollowUpTemplate(step.message, enrollee)
                val success = SmsDispatchEngine.sendSms(applicationContext, enrollee.phoneNumber, message)
                dao.insertSentLog(
                    FollowUpSentLogEntity(enrolleeId = enrollee.id, stepId = step.id, success = success)
                )
            }

            val totalSent = alreadySent.size + due.size
            if (totalSent >= steps.size) {
                dao.updateEnrolleeStatus(enrollee.id, com.tgcrongai.givingapp.data.FollowUpEnrolleeStatus.COMPLETED)
            }
        }

        return Result.success()
    }
}
