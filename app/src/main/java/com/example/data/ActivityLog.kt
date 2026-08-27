package com.example.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "activity_logs")
data class ActivityLog(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int = 0,

    @ColumnInfo(name = "type")
    val type: String, // e.g., "ORDER_CREATED", "PAYMENT_RECORDED", "STATUS_CHANGE"

    @ColumnInfo(name = "description")
    val description: String,

    @ColumnInfo(name = "targetId")
    val targetId: Int? = null, // ID of the order or payment involved

    @ColumnInfo(name = "timestamp")
    val timestamp: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "isSynced")
    val isSynced: Boolean = false
)
