package com.example.watchgps.presentation

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import com.example.watchgps.presentation.theme.WatchGPSTheme
import com.google.android.gms.location.*
import androidx.wear.compose.material.TimeText
import androidx.wear.tooling.preview.devices.WearDevices

class MainActivity : ComponentActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    // 위치 상태를 UI에 표시하기 위한 상태 변수
    private val locationText = mutableStateOf("위치 정보 없음")

    private val locationPermissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    private val locationPermissionRequest by lazy {
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true) {
                getLastLocation()
            } else {
                locationText.value = "위치 권한이 필요합니다"
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // 권한 요청 후 위치 요청
        locationPermissionRequest.launch(locationPermissions)

        setContent {
            WearApp(
                locationText = locationText.value,
                onLocationRequest = { getLastLocation() }
            )
        }
    }

    private fun getLastLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            locationText.value = "위치 권한 없음"
            return
        }

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                location?.let {
                    val lat = it.latitude
                    val lon = it.longitude
                    locationText.value = "위도: $lat\n경도: $lon"
                } ?: run {
                    locationText.value = "위치 정보 없음"
                }
            }
            .addOnFailureListener {
                locationText.value = "위치 가져오기 실패"
            }
    }
}

@Composable
fun WearApp(
    greetingName: String,
    onLocationRequest: () -> Unit,
    locationText: String,
    hasPermission: Boolean
) {
    WatchGPSTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.background),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                TimeText()
                Spacer(modifier = Modifier.height(8.dp))

                Greeting(greetingName = greetingName)

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = locationText,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.body2
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onLocationRequest,
                    modifier = Modifier.fillMaxWidth(0.8f)
                ) {
                    Text(
                        text = if (hasPermission) "GPS 위치 가져오기" else "권한 요청",
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun WearApp(
    locationText: String,
    onLocationRequest: () -> Unit
) {
    WatchGPSTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                TimeText()
                Text(
                    text = locationText,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(8.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = onLocationRequest) {
                    Text("GPS 위치 가져오기")
                }
            }
        }
    }
}

@Preview(device = WearDevices.SMALL_ROUND, showSystemUi = true)
@Composable
fun DefaultPreview() {
    WearApp("위치 정보 없음", onLocationRequest = {})
}

@Composable
fun Greeting(greetingName: String) {
    Text(
        text = "Hello $greetingName!",
        color = Color.White,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(8.dp)
    )
}