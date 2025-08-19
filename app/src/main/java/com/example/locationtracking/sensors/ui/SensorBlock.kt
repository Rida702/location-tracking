package com.example.locationtracking.sensors.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import kotlinx.coroutines.flow.StateFlow
import kotlin.math.*

@Composable
fun SensorBlock(
    title: String,
    valuesFlow: StateFlow<Triple<Float, Float, Float>>,
    isListening: Boolean,
    onStartStop: () -> Unit,
    isAvailable: Boolean = true
) {
    val values = valuesFlow.collectAsState()

    // Calculate motion intensity for color change with noise filtering
    val motionIntensity = if (isAvailable) {
        val magnitude = sqrt(
            values.value.first.pow(2) +
                    values.value.second.pow(2) +
                    values.value.third.pow(2)
        )
        // Apply threshold to filter noise (adjust these values as needed)
        when {
            title.contains("Accelerometer") -> {
                // For accelerometer, subtract gravity (~9.8) and apply threshold
                val netAcceleration = abs(magnitude - 9.8f)
                when {
                    netAcceleration > 3.0f -> 3 // High motion - Red
                    netAcceleration > 1.5f -> 2 // Medium motion - Orange
                    netAcceleration > 0.5f -> 1 // Low motion - Yellow
                    else -> 0 // Stationary - White
                }
            }
            else -> {
                // For gyroscope
                when {
                    magnitude > 2.0f -> 3 // High rotation - Red
                    magnitude > 1.0f -> 2 // Medium rotation - Orange
                    magnitude > 0.3f -> 1 // Low rotation - Yellow
                    else -> 0 // No rotation - White
                }
            }
        }
    } else 0

    val backgroundColor = when (motionIntensity) {
        3 -> Color(0xFFFFCDD2) // Light Red
        2 -> Color(0xFFFFE0B2) // Light Orange
        1 -> Color(0xFFFFF9C4) // Light Yellow
        else -> Color.White
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(
            modifier = Modifier
                .background(backgroundColor)
                .padding(16.dp)
        ) {
            // Title and Start/Stop Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                if (isAvailable) {
                    Button(
                        onClick = onStartStop,
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text(
                            text = if (isListening) "Stop" else "Start",
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (!isAvailable) {
                Text(
                    text = "❌ Sensor not available on this device",
                    color = Color.Red,
                    fontSize = 14.sp
                )
            } else if (!isListening) {
                Text(
                    text = "⏸️ Sensor stopped",
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            } else {
                // Raw values
                Text(
                    text = "Raw Values:",
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    fontSize = 14.sp
                )
                Text(text = "X: ${String.format("%.3f", values.value.first)}", color = Color.Black)
                Text(text = "Y: ${String.format("%.3f", values.value.second)}", color = Color.Black)
                Text(text = "Z: ${String.format("%.3f", values.value.third)}", color = Color.Black)

                Spacer(modifier = Modifier.height(8.dp))

                // Calculated values
                if (title.contains("Accelerometer")) {
                    val magnitude = sqrt(
                        values.value.first.pow(2) +
                                values.value.second.pow(2) +
                                values.value.third.pow(2)
                    )
                    val netAcceleration = magnitude - 9.8f

                    Text(
                        text = "Acceleration Info:",
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Total Magnitude: ${String.format("%.3f", magnitude)} m/s²",
                        color = Color.Black
                    )
                    Text(
                        text = "Net Acceleration: ${String.format("%.3f", abs(netAcceleration))} m/s²",
                        color = Color.Black
                    )

                    // Dominant direction
                    val dominantAxis = when {
                        abs(values.value.first) > abs(values.value.second) &&
                                abs(values.value.first) > abs(values.value.third) -> {
                            if (values.value.first > 0) "+X (Right)" else "-X (Left)"
                        }
                        abs(values.value.second) > abs(values.value.third) -> {
                            if (values.value.second > 0) "+Y (Forward)" else "-Y (Backward)"
                        }
                        else -> {
                            if (values.value.third > 0) "+Z (Up)" else "-Z (Down)"
                        }
                    }
                    Text(
                        text = "Dominant Direction: $dominantAxis",
                        color = Color.Black
                    )

                } else if (title.contains("Gyroscope")) {
                    val angularMagnitude = sqrt(
                        values.value.first.pow(2) +
                                values.value.second.pow(2) +
                                values.value.third.pow(2)
                    )

                    Text(
                        text = "Rotation Info:",
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Angular Velocity: ${String.format("%.3f", angularMagnitude)} rad/s",
                        color = Color.Black
                    )
                    Text(
                        text = "Degrees/sec: ${String.format("%.1f", Math.toDegrees(angularMagnitude.toDouble()))}°/s",
                        color = Color.Black
                    )

                    // Rotation direction
                    val rotationAxis = when {
                        abs(values.value.first) > abs(values.value.second) &&
                                abs(values.value.first) > abs(values.value.third) -> {
                            if (values.value.first > 0) "Pitch (X+)" else "Pitch (X-)"
                        }
                        abs(values.value.second) > abs(values.value.third) -> {
                            if (values.value.second > 0) "Roll (Y+)" else "Roll (Y-)"
                        }
                        else -> {
                            if (values.value.third > 0) "Yaw (Z+)" else "Yaw (Z-)"
                        }
                    }
                    Text(
                        text = "Primary Rotation: $rotationAxis",
                        color = Color.Black
                    )
                }
            }
        }
    }
}