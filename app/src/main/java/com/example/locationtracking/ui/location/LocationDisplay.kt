package com.example.locationtracking.ui.location

import android.location.Location
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun LocationDisplay(
    locationFlow: Flow<Location>,
    isPermissionGranted: Boolean,
    modifier: Modifier = Modifier
) {
    var currentLocation by remember { mutableStateOf<Location?>(null) }
    var lastUpdateTime by remember { mutableStateOf<String?>(null) }
    var locationCount by remember { mutableIntStateOf(0) }

    // Collect location updates
    LaunchedEffect(isPermissionGranted) {
        if (isPermissionGranted) {
            locationFlow.collect { location ->
                currentLocation = location
                locationCount++
                val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                lastUpdateTime = timeFormat.format(Date(location.time))
            }
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(
            modifier = Modifier
                .background(Color.White)
                .padding(16.dp)
        ) {
            Text(
                text = "📍 GPS Location",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (!isPermissionGranted) {
                Text(
                    text = "❌ Location permission not granted",
                    color = Color.Red,
                    fontSize = 14.sp
                )
            } else if (currentLocation == null) {
                Text(
                    text = "🔍 Searching for GPS signal...",
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            } else {
                // Display current location
                Text(
                    text = "Current Position:",
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    fontSize = 14.sp
                )

                Text(
                    text = "Latitude: ${String.format("%.6f", currentLocation!!.latitude)}°",
                    color = Color.Black,
                    fontSize = 14.sp
                )

                Text(
                    text = "Longitude: ${String.format("%.6f", currentLocation!!.longitude)}°",
                    color = Color.Black,
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Additional location info
                Text(
                    text = "Location Details:",
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    fontSize = 14.sp
                )

                currentLocation?.let { location ->
                    if (location.hasAccuracy()) {
                        Text(
                            text = "Accuracy: ±${String.format("%.1f", location.accuracy)}m",
                            color = Color.Black,
                            fontSize = 12.sp
                        )
                    }

                    if (location.hasAltitude()) {
                        Text(
                            text = "Altitude: ${String.format("%.1f", location.altitude)}m",
                            color = Color.Black,
                            fontSize = 12.sp
                        )
                    }

                    if (location.hasSpeed()) {
                        val speedKmh = location.speed * 3.6f // Convert m/s to km/h
                        Text(
                            text = "Speed: ${String.format("%.1f", speedKmh)} km/h",
                            color = Color.Black,
                            fontSize = 12.sp
                        )
                    }

                    if (location.hasBearing()) {
                        val direction = getDirectionFromBearing(location.bearing)
                        Text(
                            text = "Direction: ${String.format("%.1f", location.bearing)}° ($direction)",
                            color = Color.Black,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Status info
                Text(
                    text = "Status:",
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    fontSize = 14.sp
                )

                lastUpdateTime?.let { time ->
                    Text(
                        text = "Last Update: $time",
                        color = Color.Black,
                        fontSize = 12.sp
                    )
                }

                Text(
                    text = "Updates Received: $locationCount",
                    color = Color.Black,
                    fontSize = 12.sp
                )

                currentLocation?.provider?.let { provider ->
                    Text(
                        text = "Provider: ${provider.uppercase()}",
                        color = Color.Black,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

private fun getDirectionFromBearing(bearing: Float): String {
    return when {
        bearing < 22.5 || bearing >= 337.5 -> "N"
        bearing < 67.5 -> "NE"
        bearing < 112.5 -> "E"
        bearing < 157.5 -> "SE"
        bearing < 202.5 -> "S"
        bearing < 247.5 -> "SW"
        bearing < 292.5 -> "W"
        bearing < 337.5 -> "NW"
        else -> "N"
    }
}