package com.tgcrongai.givingapp.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Query("SELECT * FROM transactions ORDER BY receivedTimestamp DESC")
    fun observeAll(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE transactionId = :id LIMIT 1")
    suspend fun getById(id: String): TransactionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: TransactionEntity)

    @Update
    suspend fun update(transaction: TransactionEntity)

    @Query("UPDATE transactions SET purpose = :purpose, status = :status WHERE transactionId = :id")
    suspend fun tagPurpose(id: String, purpose: String, status: String)

    @Query("UPDATE transactions SET status = :status, sentTimestamp = :sentAt WHERE transactionId = :id")
    suspend fun updateStatus(id: String, status: String, sentAt: Long?)

    @Query("UPDATE transactions SET retryCount = retryCount + 1 WHERE transactionId = :id")
    suspend fun incrementRetry(id: String)

    @Query("SELECT * FROM transactions WHERE syncedOnline = 0")
    suspend fun getUnsyncedTransactions(): List<TransactionEntity>

    @Query("UPDATE transactions SET syncedOnline = 1 WHERE transactionId = :id")
    suspend fun markAsSynced(id: String)

    @Query("SELECT COUNT(*) FROM transactions")
    suspend fun getTotalCount(): Int

    @Query("SELECT COUNT(*) FROM transactions WHERE syncedOnline = 0")
    suspend fun getUnsyncedCount(): Int
}
