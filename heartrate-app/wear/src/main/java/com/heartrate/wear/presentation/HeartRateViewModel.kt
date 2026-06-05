package com.heartrate.wear.presentation

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.heartrate.wear.health.HeartRateForegroundService
import com.heartrate.wear.health.HeartRateManager
import com.heartrate.wear.health.HeartRateState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HeartRateViewModel(application: Application) : AndroidViewModel(application) {

    private val heartRateManager = HeartRateManager(application)

    private val _uiState = MutableStateFlow(HeartRateUiState())
    val uiState: StateFlow<HeartRateUiState> = _uiState.asStateFlow()

    init {
        checkCapabilityAndStart()
    }

    private fun checkCapabilityAndStart() {
        viewModelScope.launch {
            val hasCapability = heartRateManager.hasHeartRateCapability()
            if (!hasCapability) {
                _uiState.value = HeartRateUiState(error = "이 기기는 심박수 측정을 지원하지 않습니다.")
                return@launch
            }
            _uiState.value = HeartRateUiState(isCapable = true)
        }
    }

    fun startMonitoring() {
        val context = getApplication<Application>()
        val serviceIntent = Intent(context, HeartRateForegroundService::class.java)
        context.startForegroundService(serviceIntent)

        viewModelScope.launch {
            heartRateManager.heartRateFlow().collect { state ->
                _uiState.value = _uiState.value.copy(
                    heartRate = state,
                    isMonitoring = true,
                    error = null
                )
            }
        }
    }

    fun stopMonitoring() {
        val context = getApplication<Application>()
        context.stopService(Intent(context, HeartRateForegroundService::class.java))
        _uiState.value = _uiState.value.copy(isMonitoring = false)
    }
}

data class HeartRateUiState(
    val heartRate: HeartRateState = HeartRateState(),
    val isMonitoring: Boolean = false,
    val isCapable: Boolean = false,
    val error: String? = null
)
