package com.heartrate.mobile.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.wearable.Wearable
import com.heartrate.mobile.data.HeartRateData
import com.heartrate.mobile.data.HeartRateHistory
import com.heartrate.mobile.data.HeartRateRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class HeartRateViewModel(application: Application) : AndroidViewModel(application) {

    val currentHeartRate: StateFlow<HeartRateData> = HeartRateRepository.currentHeartRate
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HeartRateData())

    val history: StateFlow<List<HeartRateHistory>> = HeartRateRepository.history
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val isConnected: StateFlow<Boolean> = HeartRateRepository.isConnected
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    init {
        checkWearableConnection()
    }

    private fun checkWearableConnection() {
        viewModelScope.launch {
            try {
                val nodes = Wearable.getNodeClient(getApplication<Application>())
                    .connectedNodes.await()
                HeartRateRepository.setConnected(nodes.isNotEmpty())
            } catch (e: Exception) {
                HeartRateRepository.setConnected(false)
            }
        }
    }

    fun refreshConnection() {
        checkWearableConnection()
    }
}
