package com.example.locationtracking

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.example.locationtracking.ui.theme.LocationtrackingTheme
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.example.locationtracking.ui.permissions.PermissionStatusScreen
import com.example.locationtracking.ui.location.LocationDisplay
import com.example.locationtracking.ui.location.LocationUpdater
import com.example.locationtracking.sensors.ui.SensorBlock

import android.content.Intent
import android.os.Build
import com.example.locationtracking.sensors.AccelerometerManager
import com.example.locationtracking.sensors.GyroscopeManager
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.*

import com.example.locationtracking.service.LocationForegroundService


class MainActivity : ComponentActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var accelerometerManager: AccelerometerManager
    private lateinit var gyroscopeManager: GyroscopeManager
    private lateinit var locationUpdater: LocationUpdater

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Fused Location Provider
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Initialize sensors
        accelerometerManager = AccelerometerManager(this)
        gyroscopeManager = GyroscopeManager(this)

        // Initialize location updater
        locationUpdater = LocationUpdater(this)

        // Start sensors initially
        accelerometerManager.startListening()
        gyroscopeManager.startListening()

        enableEdgeToEdge()

        setContent {
            LocationtrackingTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->

                    var permissionsGranted by remember { mutableStateOf(false) }

                    Column(
                        modifier = Modifier
                            .padding(innerPadding)
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()) // Add scrolling for better UX
                    ) {
                        // Permission Status
                        PermissionStatusScreen(
                            fusedLocationClient = fusedLocationClient,
                            modifier = Modifier,
                            onPermissionsGranted = {
                                permissionsGranted = true
                                // Start foreground service
                                val intent = Intent(this@MainActivity, LocationForegroundService::class.java)
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    startForegroundService(intent)
                                } else {
                                    startService(intent)
                                }
                            }
                        )

                        // GPS Location Display
                        LocationDisplay(
                            locationFlow = locationUpdater.getLocationUpdates(),
                            isPermissionGranted = permissionsGranted,
                            modifier = Modifier
                        )

                        // Sensor UI blocks with state management
                        val accelerometerListening = accelerometerManager.isListening.collectAsState()
                        val gyroscopeListening = gyroscopeManager.isListening.collectAsState()

                        SensorBlock(
                            title = "Accelerometer",
                            valuesFlow = accelerometerManager.values,
                            isListening = accelerometerListening.value,
                            onStartStop = { accelerometerManager.toggleListening() },
                            isAvailable = accelerometerManager.isAvailable
                        )

                        SensorBlock(
                            title = "Gyroscope",
                            valuesFlow = gyroscopeManager.values,
                            isListening = gyroscopeListening.value,
                            onStartStop = { gyroscopeManager.toggleListening() },
                            isAvailable = gyroscopeManager.isAvailable
                        )
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Only restart if they were previously listening
        if (accelerometerManager.isAvailable) {
            accelerometerManager.startListening()
        }
        if (gyroscopeManager.isAvailable) {
            gyroscopeManager.startListening()
        }
    }

    override fun onPause() {
        super.onPause()
        accelerometerManager.stopListening()
        gyroscopeManager.stopListening()
    }

    override fun onDestroy() {
        super.onDestroy()
        accelerometerManager.stopListening()
        gyroscopeManager.stopListening()
    }
}