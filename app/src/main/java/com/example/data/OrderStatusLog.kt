package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "order_status_logs")
data class OrderStatusLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val orderId: Int,
    val previousStatus: String,
    val newStatus: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false
)
