package com.heartrate.wear.health

import android.content.Context
import androidx.health.services.client.HealthServices
import androidx.health.services.client.MeasureCallback
import androidx.health.services.client.data.Availability
import androidx.health.services.client.data.DataPointContainer
import androidx.health.services.client.data.DataType
import androidx.health.services.client.data.DeltaDataType
import androidx.health.services.client.data.SampleDataPoint
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.guava.await

data class HeartRateState(
    val bpm: Int = 0,
    val available: Boolean = false,
    val accuracy: String = "Unknown"
)

class HeartRateManager(private val context: Context) {

    private val healthServicesClient = HealthServices.getClient(context)
    private val measureClient = healthServicesClient.measureClient

    suspend fun hasHeartRateCapability(): Boolean {
        val capabilities = measureClient.getCapabilitiesAsync().await()
        return DataType.HEART_RATE_BPM in capabilities.supportedDataTypesMeasure
    }

    fun heartRateFlow(): Flow<HeartRateState> = callbackFlow {
        val callback = object : MeasureCallback {
            override fun onAvailabilityChanged(
                dataType: DeltaDataType<*, *>,
                availability: Availability
            ) {
                trySend(HeartRateState(available = availability == Availability.AVAILABLE))
            }

            override fun onDataReceived(data: DataPointContainer) {
                val heartRatePoints = data.getData(DataType.HEART_RATE_BPM)
                heartRatePoints.lastOrNull()?.let { point ->
                    val bpm = point.value.toInt()
                    val accuracy = when ((point as? SampleDataPoint)?.accuracy?.toString()) {
                        "HIGH" -> "높음"
                        "MEDIUM" -> "보통"
                        "LOW" -> "낮음"
                        else -> "측정중"
                    }
                    trySend(HeartRateState(bpm = bpm, available = true, accuracy = accuracy))
                }
            }
        }

        measureClient.registerMeasureCallbackAsync(DataType.HEART_RATE_BPM, callback).await()

        awaitClose {
            measureClient.unregisterMeasureCallbackAsync(DataType.HEART_RATE_BPM, callback)
        }
    }
}
