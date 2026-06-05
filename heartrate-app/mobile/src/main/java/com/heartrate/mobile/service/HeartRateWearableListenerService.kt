package com.heartrate.mobile.service

import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.WearableListenerService
import com.heartrate.mobile.data.HeartRateRepository

class HeartRateWearableListenerService : WearableListenerService() {

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        dataEvents.forEach { event ->
            if (event.type == DataEvent.TYPE_CHANGED &&
                event.dataItem.uri.path == "/heartrate"
            ) {
                val dataMap = DataMapItem.fromDataItem(event.dataItem).dataMap
                val bpm = dataMap.getInt("bpm")
                val accuracy = dataMap.getString("accuracy") ?: "Unknown"
                val timestamp = dataMap.getLong("timestamp")

                HeartRateRepository.updateHeartRate(bpm, accuracy, timestamp)
            }
        }
    }
}
