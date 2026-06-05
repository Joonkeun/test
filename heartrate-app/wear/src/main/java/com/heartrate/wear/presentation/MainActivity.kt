package com.heartrate.wear.presentation

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.*

class MainActivity : ComponentActivity() {

    private val viewModel: HeartRateViewModel by viewModels()

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) viewModel.startMonitoring()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WearApp(viewModel = viewModel) {
                permissionLauncher.launch(Manifest.permission.BODY_SENSORS)
            }
        }
    }
}

@Composable
fun WearApp(viewModel: HeartRateViewModel, onRequestPermission: () -> Unit) {
    val uiState by viewModel.uiState.collectAsState()

    MaterialTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            when {
                uiState.error != null -> ErrorScreen(uiState.error!!)
                uiState.isMonitoring -> HeartRateScreen(uiState) {
                    viewModel.stopMonitoring()
                }
                else -> StartScreen {
                    onRequestPermission()
                }
            }
        }
    }
}

@Composable
fun HeartRateScreen(state: HeartRateUiState, onStop: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "❤️",
            fontSize = 32.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = if (state.heartRate.bpm > 0) "${state.heartRate.bpm}" else "--",
            fontSize = 48.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Red,
            textAlign = TextAlign.Center
        )
        Text(
            text = "BPM",
            fontSize = 16.sp,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "정확도: ${state.heartRate.accuracy}",
            fontSize = 12.sp,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = onStop,
            colors = ButtonDefaults.buttonColors(backgroundColor = Color.DarkGray)
        ) {
            Text("중지", fontSize = 12.sp)
        }
    }
}

@Composable
fun StartScreen(onStart: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "심박수\n모니터",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onStart,
            colors = ButtonDefaults.buttonColors(backgroundColor = Color.Red)
        ) {
            Text("시작", fontSize = 14.sp)
        }
    }
}

@Composable
fun ErrorScreen(message: String) {
    Text(
        text = message,
        fontSize = 14.sp,
        color = Color.Yellow,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(16.dp)
    )
}
