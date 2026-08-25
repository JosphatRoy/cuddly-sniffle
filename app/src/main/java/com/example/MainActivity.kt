package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.SupervisorAccount
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.AppDatabase
import com.example.data.OrderRepository
import com.example.ui.MainViewModel
import com.example.ui.UserRole
import com.example.ui.ViewModelFactory
import com.example.ui.screens.DistributorDashboard
import com.example.ui.screens.FarmerDashboard
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Init Room database and repository
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = OrderRepository(
            database.orderDao(),
            database.paymentLogDao(),
            database.orderStatusLogDao()
        )

        setContent {
            MyApplicationTheme {
                // Instantiate ViewModel inside Compose safely
                val viewModel: MainViewModel = viewModel(
                    factory = ViewModelFactory(application, repository)
                )

                val currentRole by viewModel.userRole.collectAsState()

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    contentWindowInsets = WindowInsets.safeDrawing,
                    topBar = {
                        AppHeader(
                            currentRole = currentRole,
                            onRoleChange = { viewModel.setRole(it) }
                        )
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        AnimatedContent(
                            targetState = currentRole,
                            transitionSpec = {
                                fadeIn().togetherWith(fadeOut())
                            },
                            label = "role_switch"
                        ) { role ->
                            when (role) {
                                UserRole.FARMER -> {
                                    FarmerDashboard(
                                        viewModel = viewModel,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                                UserRole.DISTRIBUTOR -> {
                                    DistributorDashboard(
                                        viewModel = viewModel,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AppHeader(
    currentRole: UserRole,
    onRoleChange: (UserRole) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // App Title Logo Block
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Logo Badge resembling a milk drops icon
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(MaterialTheme.colorScheme.primary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "M",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Milk Delivery",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Text(
                    text = "Sales & Logistics Hub",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 36.dp)
                )
            }

            // High Fidelity Toggle Switcher
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.06f))
                    .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                    .padding(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Farmer Side
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (currentRole == UserRole.FARMER) MaterialTheme.colorScheme.primary
                            else Color.Transparent
                        )
                        .clickable { onRoleChange(UserRole.FARMER) }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .testTag("toggle_farmer_role"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.SupervisorAccount,
                        contentDescription = null,
                        tint = if (currentRole == UserRole.FARMER) Color.White else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Farmer",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (currentRole == UserRole.FARMER) Color.White else MaterialTheme.colorScheme.primary
                    )
                }

                // Distributor Side
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (currentRole == UserRole.DISTRIBUTOR) MaterialTheme.colorScheme.primary
                            else Color.Transparent
                        )
                        .clickable { onRoleChange(UserRole.DISTRIBUTOR) }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .testTag("toggle_distributor_role"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalShipping,
                        contentDescription = null,
                        tint = if (currentRole == UserRole.DISTRIBUTOR) Color.White else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Driver",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (currentRole == UserRole.DISTRIBUTOR) Color.White else MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
