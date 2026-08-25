package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "payment_logs")
data class PaymentLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val orderId: Int,
    val customerName: String,
    val amount: Double,
    val paymentMethod: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false
)
