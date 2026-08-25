package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Note
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.core.net.toUri
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.example.data.Order
import com.example.ui.DistributorTab
import com.example.ui.MainViewModel
import com.example.ui.NotificationHelper
import com.example.ui.components.CameraCaptureView
import com.example.ui.components.ProofOfDeliverySection
import com.example.ui.components.QrScannerView
import com.example.ui.components.RouteMapCanvas

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun DistributorDashboard(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val orders by viewModel.orders.collectAsState()
    val activeTab by viewModel.distributorTab.collectAsState()
    val selectedRoute by viewModel.selectedRoute.collectAsState()

    val cameraPermissionState = rememberPermissionState(android.Manifest.permission.CAMERA)

    val routeOrders = orders
        .filter { it.routeName == selectedRoute }
        .sortedBy { it.routeOrder }

    val pendingOrders = routeOrders.filter { it.status == "Pending" || it.status == "In Transit" }
    val deliveredOrders = routeOrders.filter { it.status == "Delivered" }

    var expandedRouteMenu by remember { mutableStateOf(false) }
    var selectedOrderForDelivery by remember { mutableStateOf<Order?>(null) }
    var showCamera by remember { mutableStateOf(false) }
    var showQrScanner by remember { mutableStateOf(false) }

    val context = LocalContext.current

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Route Selector Dropdown & Progress Summary Bar
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Distributor Active Route",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                    )

                    // Large Route Selector Clicker
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expandedRouteMenu = true }
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = selectedRoute,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Select Route",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = expandedRouteMenu,
                        onDismissRequest = { expandedRouteMenu = false },
                        modifier = Modifier.fillMaxWidth(0.85f)
                    ) {
                        viewModel.availableRoutes.forEach { route ->
                            DropdownMenuItem(
                                text = { Text(route, fontWeight = FontWeight.Bold) },
                                onClick = {
                                    viewModel.setSelectedRoute(route)
                                    expandedRouteMenu = false
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Progress metrics
                    val totalCount = routeOrders.count { it.status != "Cancelled" }
                    val completedCount = routeOrders.count { it.status == "Delivered" }
                    val pendingLiters = pendingOrders.sumOf { it.liters }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Progress: $completedCount of $totalCount Delivered",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Remaining: ${"%.1f".format(pendingLiters)} Liters",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Subtabs: Delivery List vs Live Route Tracker Map
            TabRow(
                selectedTabIndex = activeTab.ordinal,
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                Tab(
                    selected = activeTab == DistributorTab.DELIVERY_LIST,
                    onClick = { viewModel.setDistributorTab(DistributorTab.DELIVERY_LIST) },
                    text = { Text("Delivery List", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.AutoMirrored.Filled.ListAlt, contentDescription = "Deliveries") },
                    modifier = Modifier.testTag("tab_distributor_list")
                )
                Tab(
                    selected = activeTab == DistributorTab.ROUTE_TRACKER,
                    onClick = { viewModel.setDistributorTab(DistributorTab.ROUTE_TRACKER) },
                    text = { Text("Route Map Tracker", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.Map, contentDescription = "Route Map") },
                    modifier = Modifier.testTag("tab_distributor_map")
                )
            }

            when (activeTab) {
                DistributorTab.DELIVERY_LIST -> {
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
                                .fillMaxSize()
                                .weight(1f)
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Section: Pending deliveries
                            if (pendingOrders.isNotEmpty()) {
                                item {
                                    SectionHeader(title = "Next Deliveries")
                                }
                                items(pendingOrders, key = { it.id }) { order ->
                                    DistributorOrderCard(
                                        order = order,
                                        onTransitClick = {
                                            viewModel.updateOrderStatus(order, "In Transit")
                                            NotificationHelper.sendDispatchAlert(context, order, (15..45).random())
                                        },
                                        onDeliverClick = { selectedOrderForDelivery = order },
                                        onCallClick = { phone: String ->
                                            val intent = Intent(Intent.ACTION_DIAL).apply {
                                                data = "tel:$phone".toUri()
                                            }
                                            context.startActivity(intent)
                                        }
                                    )
                                }
                            }

                            // Section: Completed deliveries
                            if (deliveredOrders.isNotEmpty()) {
                                item {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    SectionHeader(title = "Completed Deliveries")
                                }
                                items(deliveredOrders, key = { it.id }) { order ->
                                    DistributorOrderCard(
                                        order = order,
                                        onDeliverClick = {},
                                        onCallClick = {}
                                    )
                                }
                            }
                        }
                    }
                }

                DistributorTab.ROUTE_TRACKER -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f)
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                shape = RoundedCornerShape(16.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.LocalShipping,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.secondary
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = "Route Navigation Active",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                        val nextInLine = pendingOrders.firstOrNull()
                                        Text(
                                            text = if (nextInLine != null) "Next Stop: ${nextInLine.customerName} (${nextInLine.address})"
                                                   else "All deliveries on this route completed!",
                                            fontSize = 12.sp,
                                            color = Color.Gray,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }

                        item {
                            RouteMapCanvas(routeOrders = routeOrders)
                        }
                    }
                }
            }
        }

        // Payment method popup dialog when distributor delivers milk
        if (selectedOrderForDelivery != null && !showCamera && !showQrScanner) {
            val order = selectedOrderForDelivery!!
            var selectedPaymentMethod by remember { mutableStateOf("Cash") }
            var capturedPhotoUri by remember { mutableStateOf<String?>(null) }
            var scannedQrCode by remember { mutableStateOf<String?>(null) }
            var signatureCaptured by remember { mutableStateOf(false) }

            AlertDialog(
                onDismissRequest = { selectedOrderForDelivery = null },
                title = {
                    Text(
                        text = "Confirm Milk Delivery",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    LazyColumn(modifier = Modifier.fillMaxWidth()) {
                        item {
                            Text(
                                text = "Confirm delivery of ${order.liters} Liters to ${order.customerName}.",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "Expected Amount: KSh ${"%.2f".format(order.liters * order.pricePerLiter)}",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "Select Payment Type Collected:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val paymentMethods = listOf("Cash", "Mobile Money", "Credit Card")
                                paymentMethods.forEach { method ->
                                    val isSelected = selectedPaymentMethod == method
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(
                                                if (isSelected) MaterialTheme.colorScheme.primary
                                                else MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                                            )
                                            .clickable { selectedPaymentMethod = method }
                                            .padding(vertical = 10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = method,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            ProofOfDeliverySection(
                                onPhotoClick = {
                                    if (cameraPermissionState.status.isGranted) {
                                        showCamera = true
                                    } else {
                                        cameraPermissionState.launchPermissionRequest()
                                    }
                                },
                                onQrScanClick = {
                                    if (cameraPermissionState.status.isGranted) {
                                        showQrScanner = true
                                    } else {
                                        cameraPermissionState.launchPermissionRequest()
                                    }
                                },
                                onSignatureCaptured = { signatureCaptured = it != null },
                                photoUri = capturedPhotoUri,
                                qrCode = scannedQrCode
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.markAsDelivered(
                                order = order,
                                paymentMethod = selectedPaymentMethod,
                                photoUri = capturedPhotoUri,
                                qrCode = scannedQrCode
                            )
                            NotificationHelper.sendDigitalReceipt(context, order.copy(paymentMethod = selectedPaymentMethod))
                            selectedOrderForDelivery = null
                        },
                        modifier = Modifier.testTag("distributor_confirm_delivery_button")
                    ) {
                        Text("Confirm Delivered")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { selectedOrderForDelivery = null }) {
                        Text("Cancel")
                    }
                }
            )
        }

        if (showCamera) {
            CameraCaptureView(
                onImageCaptured = { 
                    showCamera = false 
                },
                onClose = { showCamera = false },
                onError = { showCamera = false }
            )
        }

        if (showQrScanner) {
            QrScannerView(
                onCodeScanned = {
                    showQrScanner = false
                },
                onClose = { showQrScanner = false }
            )
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Black,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.padding(vertical = 4.dp)
    )
}

@Composable
fun DistributorOrderCard(
    order: Order,
    onTransitClick: () -> Unit = {},
    onDeliverClick: () -> Unit,
    onCallClick: (String) -> Unit
) {
    val isPending = order.status == "Pending"
    val isInTransit = order.status == "In Transit"
    val isActive = isPending || isInTransit

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("distributor_order_card_${order.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = if (isActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
            else Color.LightGray.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Sequence indicator & Name Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(
                                when {
                                    isInTransit -> MaterialTheme.colorScheme.tertiary
                                    isPending -> MaterialTheme.colorScheme.primary
                                    else -> Color.Gray
                                },
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${order.routeOrder + 1}",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = order.customerName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isActive) MaterialTheme.colorScheme.onSurface else Color.Gray
                    )
                }

                // Call icon (only if active and has phone)
                if (isActive && order.customerPhone.isNotBlank()) {
                    IconButton(
                        onClick = { onCallClick(order.customerPhone) },
                        modifier = Modifier.testTag("call_customer_${order.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = "Call customer",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Address Row
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = order.address,
                    fontSize = 12.sp,
                    color = Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (isInTransit) {
                Text(
                    text = "Status: IN TRANSIT 🚛",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Volume & Total Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Volume", fontSize = 10.sp, color = Color.Gray)
                    Text("${order.liters} L", fontWeight = FontWeight.Black, fontSize = 15.sp, color = if (isActive) MaterialTheme.colorScheme.primary else Color.Gray)
                }
                Column {
                    Text("Price/L", fontSize = 10.sp, color = Color.Gray)
                    Text("KSh ${"%.2f".format(order.pricePerLiter)}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.Gray)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Collectable Payment", fontSize = 10.sp, color = Color.Gray)
                    Text("KSh ${"%.2f".format(order.liters * order.pricePerLiter)}", fontWeight = FontWeight.Black, fontSize = 15.sp, color = if (isActive) MaterialTheme.colorScheme.primary else Color.Gray)
                }
            }

            // Customer delivery instructions
            if (order.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.04f), RoundedCornerShape(8.dp))
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Notes: ${order.notes}",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Action Buttons
            if (isPending) {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onTransitClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.LocalShipping, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Start Delivery (In Transit)", fontWeight = FontWeight.Bold)
                }
            } else if (isInTransit) {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onDeliverClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("mark_delivered_button_${order.id}"),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Mark as Delivered", fontWeight = FontWeight.Bold)
                }
            } else {
                // Completed tag details
                Spacer(modifier = Modifier.height(12.dp))
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Delivered",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary,
                            fontSize = 12.sp
                        )
                        Text(
                            text = "Paid via ${order.paymentMethod}",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                    
                    if (order.verifiedQrCode != null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.QrCodeScanner, null, modifier = Modifier.size(12.dp), tint = Color.Gray)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("QR Verified: ${order.verifiedQrCode}", fontSize = 10.sp, color = Color.Gray)
                        }
                    }
                }
            }
        }
    }
}
