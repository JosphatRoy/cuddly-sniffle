package com.example.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entity representing a log of a payment made for an order.
 * Includes foreign key constraints to ensure data integrity and indices for performance.
 */
@Entity(
    tableName = "payment_logs",
    foreignKeys = [
        ForeignKey(
            entity = Order::class,
            parentColumns = ["id"],
            childColumns = ["orderId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["orderId"]),
        Index(value = ["timestamp"]),
        Index(value = ["isSynced"])
    ]
)
data class PaymentLog(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int = 0,

    @ColumnInfo(name = "orderId")
    val orderId: Int,

    @ColumnInfo(name = "customerName")
    val customerName: String,

    @ColumnInfo(name = "amount")
    val amount: Double,

    @ColumnInfo(name = "paymentMethod")
    val paymentMethod: String,

    @ColumnInfo(name = "timestamp")
    val timestamp: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "isSynced")
    val isSynced: Boolean = false
)
