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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.wear.compose.material.*
import androidx.wear.tooling.preview.devices.WearDevices
import com.example.watchgps.presentation.theme.WatchGPSTheme
import com.google.android.gms.location.*

class MainActivity : ComponentActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    // Compose 상태를 위한 변수들
    private var locationText by mutableStateOf("위치 정보 없음")
    private var hasPermission by mutableStateOf(false)

    private val locationPermissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    private val locationPermissionRequest by lazy {
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            hasPermission = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                    permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

            if (hasPermission) {
                getLastLocation()
            } else {
                locationText = "위치 권한이 필요합니다"
                Toast.makeText(this, "위치 권한이 거부되었습니다", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // 현재 권한 상태 확인
        checkCurrentPermissions()

        setContent {
            WearApp(
                locationText = locationText,
                hasPermission = hasPermission,
                onLocationRequest = { handleLocationRequest() }
            )
        }
    }

    private fun checkCurrentPermissions() {
        hasPermission = ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    this, Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            getLastLocation()
        }
    }

    private fun handleLocationRequest() {
        if (hasPermission) {
            getLastLocation()
        } else {
            locationPermissionRequest.launch(locationPermissions)
        }
    }

    private fun getLastLocation() {
        if (!hasPermission) {
            locationText = "위치 권한 없음"
            return
        }

        locationText = "위치 가져오는 중..."

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                location?.let {
                    val lat = String.format("%.6f", it.latitude)
                    val lon = String.format("%.6f", it.longitude)
                    locationText = "위도: $lat\n경도: $lon"
                    Toast.makeText(this, "위치 업데이트 완료", Toast.LENGTH_SHORT).show()
                } ?: run {
                    locationText = "위치 정보를 가져올 수 없습니다"
                    Toast.makeText(this, "GPS를 켜주세요", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                locationText = "위치 가져오기 실패: ${exception.message}"
                Toast.makeText(this, "위치 서비스 오류", Toast.LENGTH_SHORT).show()
            }
    }
}

@Composable
fun WearApp(
    locationText: String,
    hasPermission: Boolean,
    onLocationRequest: () -> Unit
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
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(16.dp)
            ) {
                // 시간 표시
                TimeText(
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // 제목
                Text(
                    text = "GPS 위치 앱",
                    style = MaterialTheme.typography.title3,
                    color = MaterialTheme.colors.primary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // 위치 정보 표시
                Text(
                    text = locationText,
                    style = MaterialTheme.typography.body2,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // GPS 버튼
                Button(
                    onClick = onLocationRequest,
                    modifier = Modifier.fillMaxWidth(0.9f)
                ) {
                    Text(
                        text = if (hasPermission) "GPS 위치 가져오기" else "권한 요청하기",
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Preview(device = WearDevices.SMALL_ROUND, showSystemUi = true)
@Composable
fun DefaultPreview() {
    WearApp(
        locationText = "위치 정보 없음",
        hasPermission = false,
        onLocationRequest = {}
    )
}