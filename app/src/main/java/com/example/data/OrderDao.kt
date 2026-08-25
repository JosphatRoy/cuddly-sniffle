package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface OrderDao {
    @Query("SELECT * FROM orders ORDER BY routeName ASC, routeOrder ASC")
    fun getAllOrders(): Flow<List<Order>>

    @Query("SELECT * FROM orders WHERE routeName = :routeName ORDER BY routeOrder ASC")
    fun getOrdersByRoute(routeName: String): Flow<List<Order>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: Order)

    @Update
    suspend fun updateOrder(order: Order)

    @Query("DELETE FROM orders WHERE id = :id")
    suspend fun deleteOrderById(id: Int)

    @Query("DELETE FROM orders")
    suspend fun deleteAll()

    @Query("SELECT * FROM orders WHERE isSynced = 0")
    suspend fun getUnsyncedOrders(): List<Order>

    @Query("UPDATE orders SET isSynced = 1 WHERE id = :orderId")
    suspend fun markAsSynced(orderId: Int)
}
