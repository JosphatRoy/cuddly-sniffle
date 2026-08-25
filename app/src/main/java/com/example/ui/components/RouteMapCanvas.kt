package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Order
import kotlin.math.pow

@Composable
fun RouteMapCanvas(
    routeOrders: List<Order>,
    modifier: Modifier = Modifier
) {
    val themePrimary = MaterialTheme.colorScheme.primary
    val themeSecondary = MaterialTheme.colorScheme.secondary
    val themeTertiary = MaterialTheme.colorScheme.tertiary
    val themeBackground = MaterialTheme.colorScheme.background

    // Infinite transition for pulsing glowing effect
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 8f,
        targetValue = 20f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    // Simulation animation: Truck moving towards next delivery
    val truckMovement by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "truckMovement"
    )

    if (routeOrders.isEmpty()) {
        Box(
            modifier = modifier
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
                .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No deliveries scheduled for this route.",
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                style = MaterialTheme.typography.bodyMedium
            )
        }
        return
    }

    val totalNodes = routeOrders.size + 1
    val canvasHeight = (totalNodes * 130).dp

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(canvasHeight)
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(24.dp))
            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), RoundedCornerShape(24.dp))
            .padding(16.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxWidth().height(canvasHeight)) {
            val width = size.width
            val hqOffset = Offset(width / 2f, 80f)
            val points = mutableListOf<Offset>()
            points.add(hqOffset)

            routeOrders.forEachIndexed { index, _ ->
                val step = index + 1
                val y = 80f + step * 250f
                val x = if (step % 2 == 1) width * 0.25f else width * 0.75f
                points.add(Offset(x, y))
            }

            val roadPath = Path().apply {
                moveTo(points[0].x, points[0].y)
                for (i in 1 until points.size) {
                    val pPrev = points[i - 1]
                    val pCurr = points[i]
                    val controlY = (pPrev.y + pCurr.y) / 2f
                    cubicTo(pPrev.x, controlY, pCurr.x, controlY, pCurr.x, pCurr.y)
                }
            }

            drawPath(path = roadPath, color = themeSecondary.copy(alpha = 0.15f), style = Stroke(width = 30f))
            drawPath(path = roadPath, color = themeSecondary.copy(alpha = 0.6f), style = Stroke(width = 4f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 15f), 0f)))

            // Draw landscape elements
            points.forEachIndexed { index, point ->
                if (index > 0) {
                    val treePos = Offset(point.x + (if (index % 2 == 0) -60f else 60f), point.y - 40f)
                    drawTree(treePos, themeSecondary.copy(alpha = 0.3f))
                }
            }

            // HQ
            drawCircle(color = themePrimary, radius = 32f, center = hqOffset)
            drawCircle(color = themeTertiary, radius = 16f, center = hqOffset)

            val activeIndexInOrders = routeOrders.indexOfFirst { it.status == "Pending" || it.status == "In Transit" }
            val activeNodeIndex = if (activeIndexInOrders != -1) activeIndexInOrders + 1 else -1

            // Moving truck simulation
            if (activeNodeIndex != -1) {
                val startPoint = points[activeNodeIndex - 1]
                val endPoint = points[activeNodeIndex]
                val t = truckMovement
                val controlY = (startPoint.y + endPoint.y) / 2f
                
                val currentX = (1 - t).pow(3) * startPoint.x + 3 * (1 - t).pow(2) * t * startPoint.x + 3 * (1 - t) * t.pow(2) * endPoint.x + t.pow(3) * endPoint.x
                val currentY = (1 - t).pow(3) * startPoint.y + 3 * (1 - t).pow(2) * t * controlY + 3 * (1 - t) * t.pow(2) * controlY + t.pow(3) * endPoint.y
                
                drawTruck(Offset(currentX, currentY), themePrimary.copy(alpha = 0.5f), themeTertiary.copy(alpha = 0.5f))
            }

            routeOrders.forEachIndexed { index, order ->
                val nodeOffset = points[index + 1]
                val isTarget = index + 1 == activeNodeIndex

                val circleColor = when (order.status) {
                    "Delivered" -> themeSecondary
                    "Cancelled" -> Color.Gray
                    else -> if (isTarget) themeTertiary else themeBackground
                }

                if (isTarget) {
                    drawCircle(color = themeTertiary.copy(alpha = 0.4f), radius = 35f + pulseScale, center = nodeOffset)
                }

                drawCircle(color = circleColor, radius = 28f, center = nodeOffset)
                drawCircle(color = themePrimary.copy(alpha = if (isTarget) 1f else 0.5f), radius = 28f, center = nodeOffset, style = Stroke(width = 4f))

                if (order.status == "Delivered") {
                    drawCheck(nodeOffset, Color.White)
                } else if (isTarget) {
                    drawTruck(nodeOffset, themePrimary, themeTertiary)
                }
            }
        }

        Text(
            text = "Githunguri HQ",
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            color = themePrimary,
            modifier = Modifier.align(Alignment.TopCenter).padding(top = 45.dp)
        )

        routeOrders.forEachIndexed { index, order ->
            val step = index + 1
            val isLeft = step % 2 == 1
            val topOffset = (80f + step * 250f) / 2.7f

            Box(modifier = Modifier.fillMaxWidth().padding(top = topOffset.dp)) {
                Column(
                    modifier = Modifier.fillMaxWidth(0.42f).padding(horizontal = 8.dp).align(if (isLeft) Alignment.CenterEnd else Alignment.CenterStart),
                    horizontalAlignment = if (isLeft) Alignment.Start else Alignment.End
                ) {
                    Text(text = "${order.routeOrder + 1}. ${order.customerName}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = if (order.status == "Cancelled") Color.Gray else MaterialTheme.colorScheme.onSurface, maxLines = 1)
                    Text(text = "${order.liters}L • ${order.address}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), maxLines = 1)
                }
            }
        }
    }
}

private fun DrawScope.drawTruck(offset: Offset, color: Color, wheelColor: Color) {
    val cx = offset.x
    val cy = offset.y
    val truckPath = Path().apply {
        moveTo(cx - 12f, cy + 8f)
        lineTo(cx - 12f, cy - 6f)
        lineTo(cx + 2f, cy - 6f)
        lineTo(cx + 2f, cy - 2f)
        lineTo(cx + 12f, cy - 2f)
        lineTo(cx + 12f, cy + 8f)
        close()
    }
    drawPath(path = truckPath, color = color)
    drawCircle(color = wheelColor, radius = 3f, center = Offset(cx - 6f, cy + 9f))
    drawCircle(color = wheelColor, radius = 3f, center = Offset(cx + 6f, cy + 9f))
}

private fun DrawScope.drawTree(offset: Offset, color: Color) {
    val cx = offset.x
    val cy = offset.y
    val treePath = Path().apply {
        moveTo(cx, cy - 15f)
        lineTo(cx + 10f, cy + 5f)
        lineTo(cx - 10f, cy + 5f)
        close()
    }
    drawPath(path = treePath, color = color)
    drawRect(color = color, topLeft = Offset(cx - 2f, cy + 5f), size = androidx.compose.ui.geometry.Size(4f, 8f))
}

private fun DrawScope.drawCheck(offset: Offset, color: Color) {
    val checkPath = Path().apply {
        moveTo(offset.x - 10f, offset.y)
        lineTo(offset.x - 3f, offset.y + 8f)
        lineTo(offset.x + 12f, offset.y - 8f)
    }
    drawPath(path = checkPath, color = color, style = Stroke(width = 5f))
}
