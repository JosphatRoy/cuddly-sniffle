package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface OrderStatusLogDao {
    @Insert
    suspend fun insert(log: OrderStatusLog): Long

    @Query("SELECT * FROM order_status_logs WHERE orderId = :orderId ORDER BY timestamp DESC")
    fun getLogsForOrder(orderId: Int): Flow<List<OrderStatusLog>>

    @Query("SELECT * FROM order_status_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<OrderStatusLog>>

    @Query("SELECT * FROM order_status_logs WHERE isSynced = 0")
    suspend fun getUnsyncedLogs(): List<OrderStatusLog>

    @Query("UPDATE order_status_logs SET isSynced = 1 WHERE id = :id")
    suspend fun markAsSynced(id: Int)
}
