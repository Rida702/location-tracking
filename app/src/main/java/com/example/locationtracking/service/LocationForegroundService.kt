package com.example.locationtracking.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.locationtracking.R
import com.example.locationtracking.ui.location.LocationUpdater
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import android.util.Log


class LocationForegroundService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.Main)
    private lateinit var locationUpdater: LocationUpdater

    companion object {
        const val CHANNEL_ID = "location_channel"
        const val NOTIFICATION_ID = 1
        private const val TAG = "LocationService"
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service onCreate")
        locationUpdater = LocationUpdater(this)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Service onStartCommand")
        val notification = buildNotification("Starting location service...")
        startForeground(NOTIFICATION_ID, notification)

        serviceScope.launch {
            locationUpdater.getLocationUpdates().collectLatest { location ->
                val updatedNotification = buildNotification(
                    "Lat: ${location.latitude}, Lon: ${location.longitude}"
                )
                val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                manager.notify(NOTIFICATION_ID, updatedNotification)
            }
        }

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

//    private fun createNotificationChannel() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            val channel = NotificationChannel(
//                CHANNEL_ID,
//                "Location Service",
//                NotificationManager.IMPORTANCE_DEFAULT
//            )
//            val manager = getSystemService(NotificationManager::class.java)
//            manager.createNotificationChannel(channel)
//        }
//    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Location Service",
                NotificationManager.IMPORTANCE_LOW // LOW = no sound, no vibration, no popup
            ).apply {
                description = "Shows current GPS coordinates"
                setShowBadge(false)        // No badge on app icon
                enableLights(false)        // No LED light
                enableVibration(false)     // No vibration
                setSound(null, null)       // No sound
                setBypassDnd(false)        // Don't bypass Do Not Disturb
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }

            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
            Log.d(TAG, "Silent notification channel created")
        }
    }

    private fun buildNotification(content: String): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Location Tracker")
            .setContentText(content)
            .setSmallIcon(R.drawable.ic_location) // Make sure to add a small icon in drawable
            .setOngoing(true)
            .build()
    }
}

class TestForegroundService : Service() {
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = NotificationCompat.Builder(this, "test_channel")
            .setContentTitle("Test Service")
            .setContentText("Service is running")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .build()

        startForeground(1, notification)
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
