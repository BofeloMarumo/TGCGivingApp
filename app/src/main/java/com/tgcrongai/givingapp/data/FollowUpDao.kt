package com.tgcrongai.givingapp.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface FollowUpDao {

    // --- sequence steps ---
    @Query("SELECT * FROM followup_steps ORDER BY dayOffset ASC")
    fun observeSteps(): Flow<List<FollowUpStepEntity>>

    @Query("SELECT * FROM followup_steps ORDER BY dayOffset ASC")
    suspend fun getStepsOnce(): List<FollowUpStepEntity>

    @Insert
    suspend fun insertStep(step: FollowUpStepEntity): Long

    @Update
    suspend fun updateStep(step: FollowUpStepEntity)

    @Delete
    suspend fun deleteStep(step: FollowUpStepEntity)

    // --- enrollees ---
    @Query("SELECT * FROM followup_enrollees ORDER BY enrolledTimestamp DESC")
    fun observeEnrollees(): Flow<List<FollowUpEnrolleeEntity>>

    @Query("SELECT * FROM followup_enrollees WHERE status = 'ACTIVE'")
    suspend fun getActiveEnrolleesOnce(): List<FollowUpEnrolleeEntity>

    @Insert
    suspend fun insertEnrollee(enrollee: FollowUpEnrolleeEntity): Long

    @Delete
    suspend fun deleteEnrollee(enrollee: FollowUpEnrolleeEntity)

    @Query("UPDATE followup_enrollees SET status = :status WHERE id = :id")
    suspend fun updateEnrolleeStatus(id: Long, status: String)

    // --- sent log ---
    @Query("SELECT * FROM followup_sent_log")
    fun observeSentLog(): Flow<List<FollowUpSentLogEntity>>

    @Query("SELECT stepId FROM followup_sent_log WHERE enrolleeId = :enrolleeId")
    suspend fun getSentStepIds(enrolleeId: Long): List<Long>

    @Insert
    suspend fun insertSentLog(log: FollowUpSentLogEntity)

    @Query("SELECT COUNT(*) FROM followup_sent_log WHERE enrolleeId = :enrolleeId AND stepId = :stepId")
    suspend fun countSentLogFor(enrolleeId: Long, stepId: Long): Int
}
