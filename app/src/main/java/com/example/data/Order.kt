package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "orders")
data class Order(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val customerName: String,
    val customerPhone: String,
    val address: String,
    val liters: Double,
    val pricePerLiter: Double,
    val routeName: String, // e.g., "North Route", "East Route", "Downtown Route"
    val routeOrder: Int = 0, // The delivery sequence position (e.g. 1st, 2nd, 3rd)
    val status: String = "Pending", // "Pending", "In Transit", "Delivered", "Cancelled"
    val paymentStatus: String = "Unpaid", // "Paid", "Unpaid"
    val paymentMethod: String = "", // "Cash", "Mobile Money", etc.
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val deliveredAt: Long? = null,
    val deliveryPhotoUri: String? = null,
    val signaturePath: String? = null,
    val verifiedQrCode: String? = null,
    val isSynced: Boolean = false,
    val lastUpdated: Long = System.currentTimeMillis()
)
