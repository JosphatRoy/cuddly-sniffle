package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PaymentLogDao {
    @Insert
    suspend fun insert(paymentLog: PaymentLog): Long

    @Query("SELECT * FROM payment_logs ORDER BY timestamp DESC")
    fun getAllPaymentLogs(): Flow<List<PaymentLog>>

    @Query("SELECT SUM(amount) FROM payment_logs")
    fun getTotalRevenue(): Flow<Double?>

    @Query("SELECT * FROM payment_logs WHERE isSynced = 0")
    suspend fun getUnsyncedPayments(): List<PaymentLog>

    @Query("UPDATE payment_logs SET isSynced = 1 WHERE id = :id")
    suspend fun markAsSynced(id: Int)
}
