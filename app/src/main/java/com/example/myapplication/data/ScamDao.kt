package com.example.myapplication.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ScamDao {
    @Query("SELECT * FROM scam_messages ORDER BY timestamp DESC")
    fun getAllScams(): Flow<List<ScamMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScam(scam: ScamMessage)

    @Delete
    suspend fun deleteScam(scam: ScamMessage)

    @Query("DELETE FROM scam_messages")
    suspend fun deleteAll()

    @Query("SELECT * FROM scam_calls ORDER BY timestamp DESC")
    fun getAllScamCalls(): Flow<List<ScamCall>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScamCall(scamCall: ScamCall)

    @Query("SELECT * FROM scam_calls WHERE phoneNumber = :phone LIMIT 1")
    suspend fun getScamCallByNumber(phone: String): ScamCall?

    // Manual Blocklist
    @Query("SELECT * FROM manual_blocks")
    fun getAllManualBlocks(): Flow<List<ManualBlock>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertManualBlock(block: ManualBlock)

    @Delete
    suspend fun deleteManualBlock(block: ManualBlock)

    @Query("SELECT EXISTS(SELECT 1 FROM manual_blocks WHERE phoneNumber = :phone)")
    suspend fun isManuallyBlocked(phone: String): Boolean
}
