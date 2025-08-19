package com.example.locationtracking.ui.permissions

import android.Manifest
import android.os.Build
import android.content.pm.PackageManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient

@Composable
fun PermissionStatusScreen(
    fusedLocationClient: FusedLocationProviderClient,
    modifier: Modifier = Modifier,
    onPermissionsGranted: () -> Unit
) {
    var foregroundPermissionGranted by remember { mutableStateOf(false) }
    var backgroundPermissionGranted by remember { mutableStateOf(false) }
    var permissionCheckComplete by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf("Checking permissions...") }

    val context = LocalContext.current

    // Foreground permission launcher
    val foregroundLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        foregroundPermissionGranted = granted
        if (granted) {
            statusMessage = "Foreground location permission granted ✅"
            // Check background permission if needed
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Will be handled by the background launcher
            } else {
                permissionCheckComplete = true
                onPermissionsGranted()
            }
        } else {
            statusMessage = "Foreground location permission denied ❌"
            permissionCheckComplete = true
        }
    }

    // Background permission launcher (API 29+)
    val backgroundLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        backgroundPermissionGranted = granted
        permissionCheckComplete = true
        if (granted) {
            statusMessage = "All location permissions granted ✅"
            onPermissionsGranted()
        } else {
            statusMessage = "Background location permission denied ❌"
        }
    }

    LaunchedEffect(Unit) {
        // Check foreground permission
        val foregroundGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        foregroundPermissionGranted = foregroundGranted

        if (!foregroundGranted) {
            statusMessage = "Requesting foreground location permission..."
            foregroundLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            // Check background permission for API 29+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val backgroundGranted = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                ) == PackageManager.PERMISSION_GRANTED

                backgroundPermissionGranted = backgroundGranted

                if (!backgroundGranted) {
                    statusMessage = "Requesting background location permission..."
                    backgroundLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                } else {
                    statusMessage = "All location permissions granted ✅"
                    permissionCheckComplete = true
                    onPermissionsGranted()
                }
            } else {
                statusMessage = "Location permission granted ✅"
                permissionCheckComplete = true
                onPermissionsGranted()
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
                text = "🔐 Permission Status",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = statusMessage,
                color = Color.Black,
                fontSize = 14.sp
            )

            if (permissionCheckComplete) {
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Permission Details:",
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    fontSize = 14.sp
                )

                // Foreground permission status
                Text(
                    text = "📍 Foreground Location: ${if (foregroundPermissionGranted) "✅ Granted" else "❌ Denied"}",
                    color = if (foregroundPermissionGranted) Color(0xFF4CAF50) else Color(0xFFF44336),
                    fontSize = 12.sp
                )

                // Background permission status (API 29+)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    Text(
                        text = "🔄 Background Location: ${if (backgroundPermissionGranted) "✅ Granted" else "❌ Denied"}",
                        color = if (backgroundPermissionGranted) Color(0xFF4CAF50) else Color(0xFFF44336),
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Service status
                if (foregroundPermissionGranted) {
                    Text(
                        text = "📡 Service Status:",
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Location service is running in foreground",
                        color = Color(0xFF4CAF50),
                        fontSize = 12.sp
                    )
                    Text(
                        text = "Check notification bar for live GPS coordinates",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                } else {
                    Text(
                        text = "⚠️ Location services unavailable",
                        color = Color(0xFFF44336),
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Please grant location permissions to continue",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}