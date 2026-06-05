package com.heartrate.mobile.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class HeartRateData(
    val bpm: Int = 0,
    val accuracy: String = "",
    val timestamp: Long = 0L,
    val timeString: String = ""
)

data class HeartRateHistory(
    val bpm: Int,
    val timeString: String
)

object HeartRateRepository {

    private val dateFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

    private val _currentHeartRate = MutableStateFlow(HeartRateData())
    val currentHeartRate: StateFlow<HeartRateData> = _currentHeartRate.asStateFlow()

    private val _history = MutableStateFlow<List<HeartRateHistory>>(emptyList())
    val history: StateFlow<List<HeartRateHistory>> = _history.asStateFlow()

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    fun updateHeartRate(bpm: Int, accuracy: String, timestamp: Long) {
        val timeString = dateFormat.format(Date(timestamp))
        _currentHeartRate.value = HeartRateData(bpm, accuracy, timestamp, timeString)
        _isConnected.value = true

        val newEntry = HeartRateHistory(bpm, timeString)
        _history.value = (listOf(newEntry) + _history.value).take(50)
    }

    fun setConnected(connected: Boolean) {
        _isConnected.value = connected
    }
}
