package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Order

@Composable
fun MilkSalesChart(
    orders: List<Order>,
    modifier: Modifier = Modifier
) {
    val themePrimary = MaterialTheme.colorScheme.primary
    val themeSecondary = MaterialTheme.colorScheme.secondary

    // Compute route totals
    val routeData = listOf(
        "Githunguri (Headquarters & Main Depot)",
        "Mombasa",
        "Nakuru",
        "Eldoret",
        "Kisumu",
        "Chaka",
        "Embu",
        "Mwingi",
        "Emali"
    ).map { route ->
        val totalLiters = orders
            .filter { it.routeName == route && it.status != "Cancelled" }
            .sumOf { it.liters }
        route to totalLiters
    }

    val maxLiters = routeData.maxOfOrNull { it.second } ?: 10.0
    val scaleMax = if (maxLiters == 0.0) 10.0 else maxLiters

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = borderStroke()
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Supply Distribution by Route",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Total scheduled volume (liters) of milk under delivery",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Bars Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                routeData.forEach { (route, liters) ->
                    val fraction = (liters / scaleMax).toFloat().coerceIn(0.01f, 1.0f)
                    val shortName = when {
                        route.contains("North") -> "North Valley"
                        route.contains("East") -> "East Ridge"
                        route.contains("Central") -> "Central Town"
                        else -> route
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        // Volume Label
                        Text(
                            text = "${"%.1f".format(liters)} L",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = themePrimary
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Custom Rounded Gradient Bar
                        Box(
                            modifier = Modifier
                                .width(28.dp)
                                .fillMaxHeight(0.8f * fraction)
                                .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                                .background(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(
                                            themePrimary,
                                            themeSecondary
                                        )
                                    )
                                )
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Route Label
                        Text(
                            text = shortName,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun borderStroke() = androidx.compose.foundation.BorderStroke(
    width = 1.dp,
    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
)
