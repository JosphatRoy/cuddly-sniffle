package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.Order
import com.example.data.OrderRepository
import com.example.sync.SyncWorker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class UserRole {
    FARMER,
    DISTRIBUTOR
}

enum class FarmerTab {
    DASHBOARD,
    ORDERS,
    MANAGEMENT,
    ROUTES
}

enum class DistributorTab {
    DELIVERY_LIST,
    ROUTE_TRACKER
}

class MainViewModel(application: Application, private val repository: OrderRepository) : AndroidViewModel(application) {

    init {
        // Prep mock data for pristine first-run layout
        viewModelScope.launch {
            repository.prepopulateIfEmpty()
        }
    }

    private fun triggerSync() {
        SyncWorker.scheduleSync(getApplication())
    }

    // Role state
    private val _userRole = MutableStateFlow(UserRole.FARMER)
    val userRole: StateFlow<UserRole> = _userRole.asStateFlow()

    fun setRole(role: UserRole) {
        _userRole.value = role
    }

    // Tabs state
    private val _farmerTab = MutableStateFlow(FarmerTab.DASHBOARD)
    val farmerTab: StateFlow<FarmerTab> = _farmerTab.asStateFlow()

    fun setFarmerTab(tab: FarmerTab) {
        _farmerTab.value = tab
    }

    private val _distributorTab = MutableStateFlow(DistributorTab.DELIVERY_LIST)
    val distributorTab: StateFlow<DistributorTab> = _distributorTab.asStateFlow()

    fun setDistributorTab(tab: DistributorTab) {
        _distributorTab.value = tab
    }

    // Route selection for both roles (distributor driving, farmer viewing)
    private val _selectedRoute = MutableStateFlow("Githunguri (Headquarters & Main Depot)")
    val selectedRoute: StateFlow<String> = _selectedRoute.asStateFlow()

    fun setSelectedRoute(route: String) {
        _selectedRoute.value = route
    }

    // Reactive orders flow
    val orders: StateFlow<List<Order>> = repository.allOrders
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val paymentLogs: StateFlow<List<com.example.data.PaymentLog>> = repository.allPaymentLogs
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val statusLogs: StateFlow<List<com.example.data.OrderStatusLog>> = repository.allStatusLogs
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Predefined routes
    val availableRoutes = listOf(
        "Githunguri (Headquarters & Main Depot)",
        "Mombasa",
        "Nakuru",
        "Eldoret",
        "Kisumu",
        "Chaka",
        "Embu",
        "Mwingi",
        "Emali"
    )

    // Order Actions
    fun addOrder(
        customerName: String,
        customerPhone: String,
        address: String,
        liters: Double,
        pricePerLiter: Double,
        routeName: String,
        notes: String
    ) {
        viewModelScope.launch {
            // Find current orders on this route to calculate next routeOrder sequence
            val currentRouteOrders = orders.value.filter { it.routeName == routeName }
            val nextSequence = if (currentRouteOrders.isEmpty()) 0 else currentRouteOrders.maxOf { it.routeOrder } + 1

            val newOrder = Order(
                customerName = customerName,
                customerPhone = customerPhone,
                address = address,
                liters = liters,
                pricePerLiter = pricePerLiter,
                routeName = routeName,
                routeOrder = nextSequence,
                notes = notes
            )
            repository.insert(newOrder)
            triggerSync()
        }
    }

    fun deleteOrder(orderId: Int) {
        viewModelScope.launch {
            repository.deleteById(orderId)
            triggerSync()
        }
    }

    fun updateOrder(order: Order) {
        viewModelScope.launch {
            repository.update(order)
            triggerSync()
        }
    }


    fun updateOrderStatus(order: Order, newStatus: String) {
        viewModelScope.launch {
            val oldStatus = order.status
            if (oldStatus != newStatus) {
                val updated = order.copy(status = newStatus)
                repository.update(updated)
                repository.logStatusChange(
                    com.example.data.OrderStatusLog(
                        orderId = order.id,
                        previousStatus = oldStatus,
                        newStatus = newStatus
                    )
                )
                triggerSync()
            }
        }
    }

    fun markAsDelivered(
        order: Order,
        paymentMethod: String,
        photoUri: String? = null,
        signaturePath: String? = null,
        qrCode: String? = null
    ) {
        viewModelScope.launch {
            val oldStatus = order.status
            val updated = order.copy(
                status = "Delivered",
                paymentStatus = "Paid",
                paymentMethod = paymentMethod,
                deliveredAt = System.currentTimeMillis(),
                deliveryPhotoUri = photoUri,
                signaturePath = signaturePath,
                verifiedQrCode = qrCode
            )
            repository.update(updated)
            
            repository.logStatusChange(
                com.example.data.OrderStatusLog(
                    orderId = order.id,
                    previousStatus = oldStatus,
                    newStatus = "Delivered"
                )
            )

            // Log the payment
            val paymentLog = com.example.data.PaymentLog(
                orderId = order.id,
                customerName = order.customerName,
                amount = order.liters * order.pricePerLiter,
                paymentMethod = paymentMethod
            )
            repository.logPayment(paymentLog)
            triggerSync()
        }
    }

    fun cancelOrder(order: Order) {
        viewModelScope.launch {
            val oldStatus = order.status
            val updated = order.copy(status = "Cancelled")
            repository.update(updated)

            repository.logStatusChange(
                com.example.data.OrderStatusLog(
                    orderId = order.id,
                    previousStatus = oldStatus,
                    newStatus = "Cancelled"
                )
            )
            triggerSync()
        }
    }

    fun updatePaymentStatus(order: Order, paymentStatus: String) {
        viewModelScope.launch {
            val updated = order.copy(paymentStatus = paymentStatus)
            repository.update(updated)
            triggerSync()
        }
    }


    // Sequence adjustment (drag/re-order)
    fun moveRouteOrderUp(order: Order) {
        viewModelScope.launch {
            val routeOrders = orders.value
                .filter { it.routeName == order.routeName }
                .sortedBy { it.routeOrder }
            
            val index = routeOrders.indexOfFirst { it.id == order.id }
            if (index > 0) {
                val prevOrder = routeOrders[index - 1]
                
                // Swap routeOrder indices
                repository.update(order.copy(routeOrder = prevOrder.routeOrder))
                repository.update(prevOrder.copy(routeOrder = order.routeOrder))
                triggerSync()
            }
        }
    }

    fun moveRouteOrderDown(order: Order) {
        viewModelScope.launch {
            val routeOrders = orders.value
                .filter { it.routeName == order.routeName }
                .sortedBy { it.routeOrder }
            
            val index = routeOrders.indexOfFirst { it.id == order.id }
            if (index >= 0 && index < routeOrders.size - 1) {
                val nextOrder = routeOrders[index + 1]
                
                // Swap routeOrder indices
                repository.update(order.copy(routeOrder = nextOrder.routeOrder))
                repository.update(nextOrder.copy(routeOrder = order.routeOrder))
                triggerSync()
            }
        }
    }

    fun optimizeRoute(routeName: String) {
        viewModelScope.launch {
            // Simulated optimization: Sort by address alphabetically as a proxy for distance/zone
            val routeOrders = orders.value
                .filter { it.routeName == routeName }
                .sortedBy { it.address }
            
            routeOrders.forEachIndexed { index, order ->
                if (order.routeOrder != index) {
                    repository.update(order.copy(routeOrder = index))
                }
            }
            triggerSync()
        }
    }
}

class ViewModelFactory(private val application: Application, private val repository: OrderRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
