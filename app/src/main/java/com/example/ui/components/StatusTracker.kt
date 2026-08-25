package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.OrderStatusLog
import com.example.ui.MainViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun StatusTracker(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val statusLogs by viewModel.statusLogs.collectAsState()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Status Timeline",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))

        if (statusLogs.isEmpty()) {
            Text(
                text = "No status changes tracked yet.",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        } else {
            LazyColumn(
                modifier = Modifier.heightIn(max = 200.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(statusLogs) { log ->
                    StatusLogItem(log = log)
                }
            }
        }
    }
}

@Composable
fun StatusLogItem(log: OrderStatusLog) {
    val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val time = dateFormat.format(Date(log.timestamp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Dot
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(MaterialTheme.colorScheme.primary, CircleShape)
        )
        
        Spacer(modifier = Modifier.width(12.dp))

        Column {
            Text(
                text = "Order #${log.orderId}: ${log.previousStatus} ➜ ${log.newStatus}",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "at $time",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray
            )
        }
    }
}
