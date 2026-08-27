package com.example.sync

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.data.AppDatabase
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class SyncWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val database = AppDatabase.getDatabase(applicationContext)
        val firestore = FirebaseFirestore.getInstance()

        return try {
            // 1. Sync Orders
            val unsyncedOrders = database.orderDao().getUnsyncedOrders()
            for (order in unsyncedOrders) {
                val orderMap = hashMapOf(
                    "id" to order.id,
                    "customerName" to order.customerName,
                    "customerPhone" to order.customerPhone,
                    "address" to order.address,
                    "liters" to order.liters,
                    "pricePerLiter" to order.pricePerLiter,
                    "routeName" to order.routeName,
                    "routeOrder" to order.routeOrder,
                    "status" to order.status,
                    "paymentStatus" to order.paymentStatus,
                    "paymentMethod" to order.paymentMethod,
                    "notes" to order.notes,
                    "driverNotes" to order.driverNotes,
                    "eta" to order.eta,
                    "timestamp" to order.timestamp,
                    "deliveredAt" to order.deliveredAt,
                    "deliveryPhotoUri" to order.deliveryPhotoUri,
                    "signaturePath" to order.signaturePath,
                    "verifiedQrCode" to order.verifiedQrCode,
                    "lastUpdated" to order.lastUpdated
                )
                
                firestore.collection("orders").document(order.id.toString())
                    .set(orderMap)
                    .await()
                
                database.orderDao().markAsSynced(order.id)
            }

            // 2. Sync Payments
            val unsyncedPayments = database.paymentLogDao().getUnsyncedPayments()
            for (payment in unsyncedPayments) {
                val paymentMap = hashMapOf(
                    "orderId" to payment.orderId,
                    "customerName" to payment.customerName,
                    "amount" to payment.amount,
                    "paymentMethod" to payment.paymentMethod,
                    "timestamp" to payment.timestamp
                )
                firestore.collection("payments").document(payment.id.toString())
                    .set(paymentMap)
                    .await()
                
                database.paymentLogDao().markAsSynced(payment.id)
            }

            // 3. Sync Status Logs
            val unsyncedLogs = database.orderStatusLogDao().getUnsyncedLogs()
            for (log in unsyncedLogs) {
                val logMap = hashMapOf(
                    "orderId" to log.orderId,
                    "previousStatus" to log.previousStatus,
                    "newStatus" to log.newStatus,
                    "timestamp" to log.timestamp
                )
                firestore.collection("status_logs").document(log.id.toString())
                    .set(logMap)
                    .await()
                
                database.orderStatusLogDao().markAsSynced(log.id)
            }

            // 4. Sync Activity Logs
            val unsyncedActivities = database.activityLogDao().getUnsyncedLogs()
            for (activity in unsyncedActivities) {
                val activityMap = hashMapOf(
                    "type" to activity.type,
                    "description" to activity.description,
                    "targetId" to activity.targetId,
                    "timestamp" to activity.timestamp
                )
                firestore.collection("activity_logs").document(activity.id.toString())
                    .set(activityMap)
                    .await()
                
                database.activityLogDao().markAsSynced(activity.id)
            }

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }

    companion object {
        fun scheduleSync(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val syncRequest = OneTimeWorkRequestBuilder<SyncWorker>()
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueue(syncRequest)
        }
    }
}
