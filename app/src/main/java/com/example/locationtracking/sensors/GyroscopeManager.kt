package com.example.locationtracking.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class GyroscopeManager(context: Context) : SensorEventListener {

    private val sensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val gyroscope: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

    private val _values = MutableStateFlow(Triple(0f, 0f, 0f))
    val values = _values.asStateFlow()

    private val _isListening = MutableStateFlow(false)
    val isListening = _isListening.asStateFlow()

    val isAvailable: Boolean get() = gyroscope != null

    fun startListening() {
        if (gyroscope != null && !_isListening.value) {
            sensorManager.registerListener(this, gyroscope, SensorManager.SENSOR_DELAY_NORMAL)
            _isListening.value = true
        }
    }

    fun stopListening() {
        if (_isListening.value) {
            sensorManager.unregisterListener(this)
            _isListening.value = false
        }
    }

    fun toggleListening() {
        if (_isListening.value) {
            stopListening()
        } else {
            startListening()
        }
    }

    override fun onSensorChanged(event: android.hardware.SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_GYROSCOPE) {
            _values.value = Triple(event.values[0], event.values[1], event.values[2])
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}