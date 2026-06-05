package com.heartrate.wear.health

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.heartrate.wear.data.WearDataSender
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class HeartRateForegroundService : Service() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var heartRateManager: HeartRateManager
    private lateinit var dataSender: WearDataSender

    companion object {
        const val CHANNEL_ID = "heart_rate_channel"
        const val NOTIFICATION_ID = 1
    }

    override fun onCreate() {
        super.onCreate()
        heartRateManager = HeartRateManager(this)
        dataSender = WearDataSender(this)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, buildNotification("심박수 측정 중..."))
        startHeartRateMonitoring()
        return START_STICKY
    }

    private fun startHeartRateMonitoring() {
        scope.launch {
            heartRateManager.heartRateFlow().collect { state ->
                if (state.available && state.bpm > 0) {
                    updateNotification("심박수: ${state.bpm} BPM")
                    dataSender.sendHeartRate(
                        bpm = state.bpm,
                        accuracy = state.accuracy,
                        timestamp = System.currentTimeMillis()
                    )
                }
            }
        }
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "심박수 모니터링",
            NotificationManager.IMPORTANCE_LOW
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    private fun buildNotification(text: String): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Heart Rate Monitor")
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .build()
    }

    private fun updateNotification(text: String) {
        val notification = buildNotification(text)
        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(NOTIFICATION_ID, notification)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }
}
