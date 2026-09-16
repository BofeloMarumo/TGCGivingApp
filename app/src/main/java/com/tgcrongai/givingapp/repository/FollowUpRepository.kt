package com.tgcrongai.givingapp.repository

import android.content.Context
import com.tgcrongai.givingapp.data.AppDatabase
import com.tgcrongai.givingapp.data.FollowUpEnrolleeEntity
import com.tgcrongai.givingapp.data.FollowUpStepEntity
import com.tgcrongai.givingapp.work.WorkScheduler
import kotlinx.coroutines.flow.Flow

/** Entry point for the Follow Up screen — entirely separate data + schedule
 *  from the everyday Giving Queue. */
class FollowUpRepository(private val context: Context) {

    private val dao = AppDatabase.getInstance(context).followUpDao()

    fun observeSteps(): Flow<List<FollowUpStepEntity>> = dao.observeSteps()
    fun observeEnrollees(): Flow<List<FollowUpEnrolleeEntity>> = dao.observeEnrollees()
    fun observeSentLog() = dao.observeSentLog()

    suspend fun addStep(dayOffset: Int, message: String) {
        dao.insertStep(FollowUpStepEntity(dayOffset = dayOffset, message = message))
    }

    suspend fun updateStep(step: FollowUpStepEntity) = dao.updateStep(step)
    suspend fun removeStep(step: FollowUpStepEntity) = dao.deleteStep(step)

    suspend fun enroll(name: String, phoneNumber: String) {
        dao.insertEnrollee(FollowUpEnrolleeEntity(name = name, phoneNumber = phoneNumber))
    }

    suspend fun unenroll(enrollee: FollowUpEnrolleeEntity) = dao.deleteEnrollee(enrollee)

    suspend fun sentStepIdsFor(enrolleeId: Long): List<Long> = dao.getSentStepIds(enrolleeId)

    /** Manually triggers the same check that otherwise runs every 6 hours. */
    fun runCheckNow() = WorkScheduler.runFollowUpCheckNow(context)

    fun ensurePeriodicCheckScheduled() = WorkScheduler.ensureFollowUpPeriodicCheck(context)
}
