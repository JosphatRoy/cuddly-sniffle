package com.example.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ProofOfDeliverySection(
    onPhotoClick: () -> Unit,
    onQrScanClick: () -> Unit,
    onSignatureCaptured: (Bitmap?) -> Unit,
    photoUri: String?,
    qrCode: String?
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Proof of Delivery", style = MaterialTheme.typography.titleMedium)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onPhotoClick,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.CameraAlt, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (photoUri != null) "Retake Photo" else "Take Photo")
            }

            Button(
                onClick = onQrScanClick,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.QrCodeScanner, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (qrCode != null) "Rescan QR" else "Scan QR")
            }
        }

        if (qrCode != null) {
            Text("Verified QR: $qrCode", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
        }

        Text("Customer Signature", style = MaterialTheme.typography.labelLarge)
        SignaturePad(onSignatureCaptured = { onSignatureCaptured(it) })
    }
}

@Composable
fun SignaturePad(
    onSignatureCaptured: (Bitmap?) -> Unit,
    modifier: Modifier = Modifier
) {
    val paths = remember { mutableStateListOf<List<Offset>>() }
    val currentPath = remember { mutableStateListOf<Offset>() }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        currentPath.add(offset)
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        currentPath.add(change.position)
                    },
                    onDragEnd = {
                        paths.add(currentPath.toList())
                        currentPath.clear()
                        // In a real app, you'd convert this to a Bitmap
                    }
                )
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            paths.forEach { path ->
                for (i in 0 until path.size - 1) {
                    drawLine(
                        color = Color.Black,
                        start = path[i],
                        end = path[i + 1],
                        strokeWidth = 4f,
                        cap = StrokeCap.Round
                    )
                }
            }
            for (i in 0 until currentPath.size - 1) {
                drawLine(
                    color = Color.Black,
                    start = currentPath[i],
                    end = currentPath[i + 1],
                    strokeWidth = 4f,
                    cap = StrokeCap.Round
                )
            }
        }
    }
}
