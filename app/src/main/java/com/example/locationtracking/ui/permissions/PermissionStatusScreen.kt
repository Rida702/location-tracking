package com.example.locationtracking.ui.permissions

import android.Manifest
import android.os.Build
import android.content.pm.PackageManager
import androidx.compose.runtime.*
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient

@Composable
fun PermissionStatusScreen(
    fusedLocationClient: FusedLocationProviderClient,
    modifier: Modifier = Modifier,
    onPermissionsGranted: () -> Unit // <-- Add this callback
) {
    var statusText by remember { mutableStateOf("Checking permissions...") }
    val context = LocalContext.current

    // Foreground permission launcher
    val foregroundLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            statusText = "Foreground location granted"
        } else {
            statusText = "Foreground location denied"
        }
    }

    // Background permission launcher (API 29+)
    val backgroundLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            statusText = "Background location granted (All the time)"
        } else {
            statusText = "Background location denied"
        }
    }

    LaunchedEffect(Unit) {
        val foregroundGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!foregroundGranted) {
            foregroundLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        // Background location for API 29+
        if (foregroundGranted && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val backgroundGranted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

            if (!backgroundGranted) {
                backgroundLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            }
        }

        // Call the callback if all permissions are granted
        if (foregroundGranted &&
            (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q ||
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_BACKGROUND_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED)
        ) {
            onPermissionsGranted()
        }
    }

    Text(text = statusText, modifier = modifier)
}
