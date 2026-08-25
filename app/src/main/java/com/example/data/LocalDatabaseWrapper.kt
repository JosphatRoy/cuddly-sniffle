package com.example.data

import android.content.Context
import kotlinx.coroutines.flow.Flow

/**
 * A basic local database wrapper that provides a simplified interface 
 * for storing and retrieving milk order entities using Room.
 */
class LocalDatabaseWrapper(context: Context) {
    private val database = AppDatabase.getDatabase(context)
    private val orderDao = database.orderDao()

    /**
     * Retrieves all orders from the local database as a reactive Flow.
     */
    fun getAllOrders(): Flow<List<Order>> {
        return orderDao.getAllOrders()
    }

    /**
     * Saves a new milk order to the local database.
     */
    suspend fun saveOrder(order: Order) {
        orderDao.insertOrder(order)
    }

    /**
     * Updates an existing milk order in the local database.
     */
    suspend fun updateOrder(order: Order) {
        orderDao.updateOrder(order)
    }

    /**
     * Deletes an order from the local database by its ID.
     */
    suspend fun deleteOrder(orderId: Int) {
        orderDao.deleteOrderById(orderId)
    }

    /**
     * Retrieves only orders that have not yet been synced to the cloud.
     */
    suspend fun getUnsyncedOrders(): List<Order> {
        return orderDao.getUnsyncedOrders()
    }

    /**
     * Marks an order as successfully synced to Firebase.
     */
    suspend fun markAsSynced(orderId: Int) {
        orderDao.markAsSynced(orderId)
    }
}
