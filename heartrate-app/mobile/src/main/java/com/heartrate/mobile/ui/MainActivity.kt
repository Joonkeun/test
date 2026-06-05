package com.heartrate.mobile.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {

    private val viewModel: HeartRateViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HeartRateApp(viewModel)
        }
    }
}

@Composable
fun HeartRateApp(viewModel: HeartRateViewModel) {
    val currentHeartRate by viewModel.currentHeartRate.collectAsState()
    val history by viewModel.history.collectAsState()
    val isConnected by viewModel.isConnected.collectAsState()

    MaterialTheme(
        colorScheme = darkColorScheme(
            background = Color(0xFF0D0D0D),
            surface = Color(0xFF1A1A1A),
            primary = Color(0xFFFF4444)
        )
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 헤더
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Heart Rate Monitor",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    ConnectionBadge(isConnected = isConnected) {
                        viewModel.refreshConnection()
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // 심박수 메인 표시
                HeartRateDisplay(
                    bpm = currentHeartRate.bpm,
                    accuracy = currentHeartRate.accuracy,
                    timeString = currentHeartRate.timeString,
                    isConnected = isConnected
                )

                Spacer(modifier = Modifier.height(24.dp))

                // BPM 상태 분류
                if (currentHeartRate.bpm > 0) {
                    HeartRateZone(bpm = currentHeartRate.bpm)
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // 측정 기록
                if (history.isNotEmpty()) {
                    Text(
                        text = "측정 기록",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Gray,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    )
                    HistoryList(history = history)
                }
            }
        }
    }
}

@Composable
fun HeartRateDisplay(
    bpm: Int,
    accuracy: String,
    timeString: String,
    isConnected: Boolean
) {
    val heartbeatAnim = rememberInfiniteTransition(label = "heartbeat")
    val scale by heartbeatAnim.animateFloat(
        initialValue = 1f,
        targetValue = if (bpm > 0) 1.15f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (bpm > 0) (60000 / maxOf(bpm, 1)) else 1000,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(220.dp)
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    colors = listOf(Color(0xFF3D0000), Color(0xFF1A0000)),
                    radius = 400f
                )
            )
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "❤️",
                fontSize = 40.sp,
                modifier = Modifier.scale(scale)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (bpm > 0) "$bpm" else "--",
                fontSize = 64.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (bpm > 0) Color(0xFFFF4444) else Color.Gray
            )
            Text(
                text = "BPM",
                fontSize = 18.sp,
                color = Color(0xFFAA0000),
                fontWeight = FontWeight.Medium
            )
            if (accuracy.isNotEmpty() && bpm > 0) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "정확도: $accuracy",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
            if (timeString.isNotEmpty()) {
                Text(
                    text = timeString,
                    fontSize = 12.sp,
                    color = Color.DarkGray
                )
            }
        }
    }
}

@Composable
fun HeartRateZone(bpm: Int) {
    val (zone, color) = when {
        bpm < 60 -> "안정" to Color(0xFF4CAF50)
        bpm < 100 -> "정상" to Color(0xFF2196F3)
        bpm < 140 -> "유산소" to Color(0xFFFF9800)
        bpm < 170 -> "고강도" to Color(0xFFFF5722)
        else -> "최대" to Color(0xFFF44336)
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.2f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "심박 구간: $zone",
                color = color,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp
            )
        }
    }
}

@Composable
fun ConnectionBadge(isConnected: Boolean, onClick: () -> Unit) {
    val bgColor by animateColorAsState(
        targetValue = if (isConnected) Color(0xFF1B5E20) else Color(0xFF4A1010),
        label = "badge_color"
    )
    val textColor = if (isConnected) Color(0xFF4CAF50) else Color(0xFFEF9A9A)

    TextButton(
        onClick = onClick,
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bgColor)
    ) {
        Text(
            text = if (isConnected) "● 연결됨" else "○ 연결 안됨",
            color = textColor,
            fontSize = 12.sp
        )
    }
}

@Composable
fun HistoryList(history: List<com.heartrate.mobile.data.HeartRateHistory>) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(history) { item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF1A1A1A))
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.timeString,
                    color = Color.Gray,
                    fontSize = 13.sp
                )
                Text(
                    text = "${item.bpm} BPM",
                    color = Color(0xFFFF6666),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
            }
        }
    }
}
