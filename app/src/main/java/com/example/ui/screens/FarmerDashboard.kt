package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.LocalActivity
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.Order
import com.example.ui.FarmerTab
import com.example.ui.MainViewModel
import com.example.ui.components.MilkSalesChart
import com.example.ui.components.OrderDialog
import com.example.ui.components.RouteMapCanvas
import com.example.ui.screens.OrderManagementDashboard

@Composable
fun FarmerDashboard(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val orders by viewModel.orders.collectAsState()
    val activeTab by viewModel.farmerTab.collectAsState()
    val selectedRoute by viewModel.selectedRoute.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Tab Row Navigation
            TabRow(
                selectedTabIndex = activeTab.ordinal,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                Tab(
                    selected = activeTab == FarmerTab.DASHBOARD,
                    onClick = { viewModel.setFarmerTab(FarmerTab.DASHBOARD) },
                    text = { Text("Dashboard", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.ShoppingBag, contentDescription = "Dashboard") },
                    modifier = Modifier.testTag("tab_farmer_dashboard")
                )
                Tab(
                    selected = activeTab == FarmerTab.ORDERS,
                    onClick = { viewModel.setFarmerTab(FarmerTab.ORDERS) },
                    text = { Text("Orders", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.LocalActivity, contentDescription = "Orders") },
                    modifier = Modifier.testTag("tab_farmer_orders")
                )
                Tab(
                    selected = activeTab == FarmerTab.MANAGEMENT,
                    onClick = { viewModel.setFarmerTab(FarmerTab.MANAGEMENT) },
                    text = { Text("Manage", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.ListAlt, contentDescription = "Management") },
                    modifier = Modifier.testTag("tab_farmer_management")
                )
                Tab(
                    selected = activeTab == FarmerTab.ROUTES,
                    onClick = { viewModel.setFarmerTab(FarmerTab.ROUTES) },
                    text = { Text("Routes", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.Route, contentDescription = "Routes") },
                    modifier = Modifier.testTag("tab_farmer_routes")
                )
            }

            when (activeTab) {
                FarmerTab.DASHBOARD -> DashboardContent(
                    orders = orders,
                    onNavigateToOrders = { viewModel.setFarmerTab(FarmerTab.ORDERS) }
                )
                FarmerTab.ORDERS -> OrdersContent(
                    orders = orders,
                    availableRoutes = viewModel.availableRoutes,
                    onUpdateStatus = { order, status ->
                        viewModel.updateOrderStatus(order, status)
                    },
                    onDeleteOrder = { orderId -> viewModel.deleteOrder(orderId) }
                )
                FarmerTab.MANAGEMENT -> OrderManagementDashboard(
                    viewModel = viewModel,
                    modifier = Modifier.fillMaxSize()
                )
                FarmerTab.ROUTES -> RoutesContent(
                    orders = orders,
                    selectedRoute = selectedRoute,
                    availableRoutes = viewModel.availableRoutes,
                    onSelectRoute = { viewModel.setSelectedRoute(it) },
                    onMoveUp = { viewModel.moveRouteOrderUp(it) },
                    onMoveDown = { viewModel.moveRouteOrderDown(it) },
                    onOptimizeRoute = { viewModel.optimizeRoute(it) }
                )
            }
        }

        // Floating Action Button to Add Order (visible on Dashboard and Orders screen)
        if (activeTab != FarmerTab.ROUTES) {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(24.dp)
                    .testTag("farmer_add_order_fab")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Order")
            }
        }

        if (showAddDialog) {
            OrderDialog(
                availableRoutes = viewModel.availableRoutes,
                onDismiss = { showAddDialog = false },
                onSave = { name, phone, address, liters, price, route, notes ->
                    viewModel.addOrder(name, phone, address, liters, price, route, notes)
                    showAddDialog = false
                }
            )
        }
    }
}

@Composable
fun DashboardContent(
    orders: List<Order>,
    onNavigateToOrders: () -> Unit
) {
    // Computations
    val totalLiters = orders.filter { it.status != "Cancelled" }.sumOf { it.liters }
    val deliveredLiters = orders.filter { it.status == "Delivered" }.sumOf { it.liters }
    
    val collectedRevenue = orders.filter { it.status == "Delivered" && it.paymentStatus == "Paid" }
        .sumOf { it.liters * it.pricePerLiter }
    val expectedRevenue = orders.filter { it.status != "Cancelled" }
        .sumOf { it.liters * it.pricePerLiter }

    val pendingCount = orders.count { it.status == "Pending" }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Image banner
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.05f))
            ) {
                // Background generated image
                Image(
                    painter = painterResource(id = R.drawable.img_milk_delivery),
                    contentDescription = "Farm hero image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Translucent Overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f))
                            )
                        )
                )

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(20.dp)
                ) {
                    Text(
                        text = "Dairy Farmer Hub",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Manage customer orders & dispatch distribution routes",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                }
            }
        }

        // Stats summary block
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Metric 1: Total Volume
                StatCard(
                    title = "Scheduled Milk",
                    value = "${"%.1f".format(totalLiters)} L",
                    subtitle = "Delivered: ${"%.1f".format(deliveredLiters)}L",
                    icon = Icons.Default.LocalShipping,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.weight(1f)
                )

                // Metric 2: Revenue
                StatCard(
                    title = "Expected Rev",
                    value = "KSh ${"%.2f".format(expectedRevenue)}",
                    subtitle = "Paid: KSh ${"%.2f".format(collectedRevenue)}",
                    icon = Icons.Default.MonetizationOn,
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Pending Active Deliveries",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Text(
                            text = "$pendingCount customer orders waiting for delivery today",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .background(MaterialTheme.colorScheme.primary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$pendingCount",
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }

        // Custom canvas sales bar-chart
        item {
            MilkSalesChart(orders = orders)
        }

        // Recent Order Activity section header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Deliveries",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "See All",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clickable { onNavigateToOrders() }
                        .padding(8.dp)
                )
            }
        }

        // List of last 3 items
        val recentOrders = orders.sortedByDescending { it.timestamp }.take(3)
        if (recentOrders.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No orders registered yet.",
                        color = Color.Gray,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        } else {
            items(recentOrders) { order ->
                OrderCompactRow(order = order)
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = color)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
fun OrdersContent(
    orders: List<Order>,
    availableRoutes: List<String>,
    onUpdateStatus: (Order, String) -> Unit,
    onDeleteOrder: (Int) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilterStatus by remember { mutableStateOf("All") }

    val filteredOrders = orders.filter { order ->
        val matchesSearch = order.customerName.contains(searchQuery, ignoreCase = true) ||
                order.address.contains(searchQuery, ignoreCase = true)
        val matchesStatus = when (selectedFilterStatus) {
            "All" -> true
            else -> order.status.equals(selectedFilterStatus, ignoreCase = true)
        }
        matchesSearch && matchesStatus
    }.sortedByDescending { it.timestamp }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Search & Filter header
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search customer or address...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("farmer_order_search_bar"),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Status Filter Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val filters = listOf("All", "Pending", "Delivered", "Cancelled")
            filters.forEach { status ->
                val isSelected = selectedFilterStatus == status
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                        )
                        .clickable { selectedFilterStatus = status }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = status,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (filteredOrders.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.ShoppingBag,
                        contentDescription = "Empty",
                        tint = Color.Gray.copy(alpha = 0.4f),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No orders match your search.",
                        color = Color.Gray
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredOrders, key = { it.id }) { order ->
                    OrderDetailedCard(
                        order = order,
                        onUpdateStatus = { status -> onUpdateStatus(order, status) },
                        onDelete = { onDeleteOrder(order.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun RoutesContent(
    orders: List<Order>,
    selectedRoute: String,
    availableRoutes: List<String>,
    onSelectRoute: (String) -> Unit,
    onMoveUp: (Order) -> Unit,
    onMoveDown: (Order) -> Unit,
    onOptimizeRoute: (String) -> Unit
) {
    val routeOrders = orders
        .filter { it.routeName == selectedRoute }
        .sortedBy { it.routeOrder }

    val totalLiters = routeOrders.filter { it.status != "Cancelled" }.sumOf { it.liters }
    val completedCount = routeOrders.count { it.status == "Delivered" }
    val totalCount = routeOrders.count { it.status != "Cancelled" }
    val progress = if (totalCount == 0) 0f else completedCount.toFloat() / totalCount.toFloat()

    var viewMode by remember { mutableStateOf("List") } // "List" or "Map"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Route Selector Row
        ScrollableTabRow(
            selectedTabIndex = availableRoutes.indexOf(selectedRoute).coerceAtLeast(0),
            edgePadding = 0.dp,
            containerColor = Color.Transparent,
            divider = {}
        ) {
            availableRoutes.forEach { route ->
                val isSelected = selectedRoute == route
                Tab(
                    selected = isSelected,
                    onClick = { onSelectRoute(route) },
                    text = { Text(route, fontWeight = FontWeight.Bold) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Route Summary Card
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Route Summary",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    FilledTonalButton(
                        onClick = { onOptimizeRoute(selectedRoute) },
                        modifier = Modifier.testTag("route_optimize_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sort,
                            contentDescription = "Optimize",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Optimize Route", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Total Volume", fontSize = 11.sp, color = Color.Gray)
                        Text("${"%.1f".format(totalLiters)} Liters", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Completed", fontSize = 11.sp, color = Color.Gray)
                        Text("$completedCount / $totalCount Delivered", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Progress Indicator
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(CircleShape),
                    color = MaterialTheme.colorScheme.secondary,
                    trackColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // View Mode Toggle
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                .padding(4.dp)
        ) {
            listOf("List", "Map").forEach { mode ->
                val isSelected = viewMode == mode
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                        .clickable { viewMode = mode }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (mode == "List") Icons.Default.ListAlt else Icons.Default.Map,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "$mode View",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (viewMode == "List") {
            Text(
                text = "Delivery Sequence (Manual adjustment)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            if (routeOrders.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No orders assigned to this route.", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(routeOrders, key = { it.id }) { order ->
                        RouteSequenceItem(
                            order = order,
                            isFirst = order.routeOrder == 0,
                            isLast = order.routeOrder == routeOrders.maxOf { it.routeOrder },
                            onMoveUp = { onMoveUp(order) },
                            onMoveDown = { onMoveDown(order) }
                        )
                    }
                }
            }
        } else {
            // Map View
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                item {
                    RouteMapCanvas(routeOrders = routeOrders)
                }
            }
        }
    }
}

@Composable
fun RouteSequenceItem(
    order: Order,
    isFirst: Boolean,
    isLast: Boolean,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Index number badge
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(
                        if (order.status == "Delivered") MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
                        else MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${order.routeOrder + 1}",
                    fontWeight = FontWeight.Black,
                    color = if (order.status == "Delivered") MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = order.customerName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${order.liters} L • ${order.address}",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Up / Down sequence triggers
            Row {
                IconButton(
                    onClick = onMoveUp,
                    enabled = !isFirst,
                    modifier = Modifier.testTag("sequence_up_${order.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowUpward,
                        contentDescription = "Move Up",
                        tint = if (isFirst) Color.LightGray else MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(
                    onClick = onMoveDown,
                    enabled = !isLast,
                    modifier = Modifier.testTag("sequence_down_${order.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowDownward,
                        contentDescription = "Move Down",
                        tint = if (isLast) Color.LightGray else MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
fun OrderCompactRow(order: Order) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.05f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = order.customerName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    text = "${order.liters} Liters • ${order.routeName}",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }

            // Status Indicator tag
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        when (order.status) {
                            "Delivered" -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
                            "Cancelled" -> Color.LightGray
                            else -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.25f)
                        }
                    )
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = order.status,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    color = when (order.status) {
                        "Delivered" -> MaterialTheme.colorScheme.secondary
                        "Cancelled" -> Color.DarkGray
                        else -> MaterialTheme.colorScheme.primary
                    }
                )
            }
        }
    }
}

@Composable
fun OrderDetailedCard(
    order: Order,
    onUpdateStatus: (String) -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("order_card_${order.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = order.customerName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = order.address, fontSize = 12.sp, color = Color.Gray, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }

                // Delete Action Button
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.testTag("order_delete_${order.id}")
                ) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.7f))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Milk Volume", fontSize = 10.sp, color = Color.Gray)
                    Text("${order.liters} Liters", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                Column {
                    Text("Total Price", fontSize = 10.sp, color = Color.Gray)
                    Text("KSh ${"%.2f".format(order.liters * order.pricePerLiter)}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Route", fontSize = 10.sp, color = Color.Gray)
                    Text(order.routeName.replace(" Route", ""), fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
                }
            }

            if (order.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.04f), RoundedCornerShape(8.dp))
                        .padding(8.dp)
                ) {
                    Text(text = "Notes: ${order.notes}", fontSize = 11.sp, color = Color.Gray)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Status indicator pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            when (order.status) {
                                "Delivered" -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
                                "Cancelled" -> Color.LightGray
                                else -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.25f)
                            }
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = order.status,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = when (order.status) {
                            "Delivered" -> MaterialTheme.colorScheme.secondary
                            "Cancelled" -> Color.DarkGray
                            else -> MaterialTheme.colorScheme.primary
                        }
                    )
                }

                // Interactive state switchers
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (order.status != "Delivered") {
                        FilledTonalButton(
                            onClick = { onUpdateStatus("Delivered") },
                            modifier = Modifier.testTag("mark_delivered_${order.id}"),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Deliver", fontSize = 11.sp)
                        }
                    }
                    if (order.status == "Pending") {
                        FilledTonalButton(
                            onClick = { onUpdateStatus("Cancelled") },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Cancel, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Cancel", fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}
