package com.heartrate.wear.data

import android.content.Context
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.tasks.await

class WearDataSender(private val context: Context) {

    private val dataClient = Wearable.getDataClient(context)

    suspend fun sendHeartRate(bpm: Int, accuracy: String, timestamp: Long) {
        val request = PutDataMapRequest.create("/heartrate").apply {
            dataMap.putInt("bpm", bpm)
            dataMap.putString("accuracy", accuracy)
            dataMap.putLong("timestamp", timestamp)
        }.asPutDataRequest().setUrgent()

        dataClient.putDataItem(request).await()
    }
}
