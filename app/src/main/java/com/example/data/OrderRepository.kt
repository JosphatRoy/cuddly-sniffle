package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class OrderRepository(
    private val orderDao: OrderDao,
    private val paymentLogDao: PaymentLogDao,
    private val orderStatusLogDao: OrderStatusLogDao
) {
    val allOrders: Flow<List<Order>> = orderDao.getAllOrders()
    val allPaymentLogs: Flow<List<PaymentLog>> = paymentLogDao.getAllPaymentLogs()
    val totalRevenue: Flow<Double?> = paymentLogDao.getTotalRevenue()
    val allStatusLogs: Flow<List<OrderStatusLog>> = orderStatusLogDao.getAllLogs()

    fun getOrdersByRoute(routeName: String): Flow<List<Order>> = orderDao.getOrdersByRoute(routeName)

    fun getStatusLogsForOrder(orderId: Int): Flow<List<OrderStatusLog>> = orderStatusLogDao.getLogsForOrder(orderId)

    suspend fun insert(order: Order) = orderDao.insertOrder(order)

    suspend fun update(order: Order) = orderDao.updateOrder(order)

    suspend fun deleteById(id: Int) = orderDao.deleteOrderById(id)

    suspend fun deleteAll() = orderDao.deleteAll()

    suspend fun logPayment(paymentLog: PaymentLog) = paymentLogDao.insert(paymentLog)

    suspend fun logStatusChange(log: OrderStatusLog) = orderStatusLogDao.insert(log)

    suspend fun prepopulateIfEmpty() {
        val currentOrders = allOrders.first()
        if (currentOrders.isEmpty()) {
            val sampleOrders = listOf(
                Order(
                    customerName = "John Doe",
                    customerPhone = "+1 555-0199",
                    address = "120 Green Hills Road",
                    liters = 5.0,
                    pricePerLiter = 120.0,
                    routeName = "Githunguri (Headquarters & Main Depot)",
                    routeOrder = 0,
                    status = "Pending",
                    paymentStatus = "Unpaid",
                    notes = "Leave on front porch cooler"
                ),
                Order(
                    customerName = "Mary Smith",
                    customerPhone = "+1 555-0144",
                    address = "240 Pine Ave",
                    liters = 10.0,
                    pricePerLiter = 120.0,
                    routeName = "Githunguri (Headquarters & Main Depot)",
                    routeOrder = 1,
                    status = "Pending",
                    paymentStatus = "Paid",
                    notes = "Ring doorbell upon arrival"
                ),
                Order(
                    customerName = "Robert Jones",
                    customerPhone = "+1 555-0188",
                    address = "405 Birch Dr",
                    liters = 3.0,
                    pricePerLiter = 120.0,
                    routeName = "Githunguri (Headquarters & Main Depot)",
                    routeOrder = 2,
                    status = "Pending",
                    paymentStatus = "Unpaid",
                    notes = "Prefers morning delivery"
                ),
                Order(
                    customerName = "David Miller",
                    customerPhone = "+1 555-0123",
                    address = "15 Oakwood Crest",
                    liters = 12.0,
                    pricePerLiter = 120.0,
                    routeName = "Mombasa",
                    routeOrder = 0,
                    status = "Pending",
                    paymentStatus = "Unpaid",
                    notes = "Beware of barking dog"
                ),
                Order(
                    customerName = "Sarah Davis",
                    customerPhone = "+1 555-0155",
                    address = "77 Elm St",
                    liters = 8.0,
                    pricePerLiter = 120.0,
                    routeName = "Mombasa",
                    routeOrder = 1,
                    status = "Delivered",
                    paymentStatus = "Paid",
                    paymentMethod = "Mobile Money",
                    notes = "Hand directly to Sarah",
                    deliveredAt = System.currentTimeMillis() - 7200000 // 2 hours ago
                ),
                Order(
                    customerName = "James Wilson",
                    customerPhone = "+1 555-0177",
                    address = "902 Main St",
                    liters = 15.0,
                    pricePerLiter = 120.0,
                    routeName = "Nakuru",
                    routeOrder = 0,
                    status = "Pending",
                    paymentStatus = "Paid",
                    notes = "Bulk commercial customer"
                ),
                Order(
                    customerName = "Patricia Taylor",
                    customerPhone = "+1 555-0166",
                    address = "14 Market Square",
                    liters = 6.0,
                    pricePerLiter = 120.0,
                    routeName = "Nakuru",
                    routeOrder = 1,
                    status = "Pending",
                    paymentStatus = "Unpaid",
                    notes = "Knock loudly, hard of hearing"
                )
            )
            for (order in sampleOrders) {
                orderDao.insertOrder(order)
                
                // If it was already delivered, log a sample payment
                if (order.status == "Delivered") {
                    paymentLogDao.insert(
                        PaymentLog(
                            orderId = 0, // In real app use actual ID
                            customerName = order.customerName,
                            amount = order.liters * order.pricePerLiter,
                            paymentMethod = order.paymentMethod
                        )
                    )
                }
            }
        }
    }
}
