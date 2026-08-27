package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ActivityLogDao {
    @Insert
    suspend fun insert(log: ActivityLog): Long

    @Query("SELECT * FROM activity_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<ActivityLog>>

    @Query("SELECT * FROM activity_logs WHERE isSynced = 0")
    suspend fun getUnsyncedLogs(): List<ActivityLog>

    @Query("UPDATE activity_logs SET isSynced = 1 WHERE id = :id")
    suspend fun markAsSynced(id: Int)
}
