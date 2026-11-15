package com.example.hw1

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat

@Composable
fun SecondScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val locationManager = remember {
        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }
    var locationText by rememberSaveable {
        mutableStateOf("Для определения координат нажмите на кнопку")
    }
    var locationListener by remember { mutableStateOf<LocationListener?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        if (result[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            result[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        ) {
            requestLocation(context, locationManager, { locationText = it }, { locationListener = it })
        } else {
            locationText = "Пожалуйста, предоставьте разрешение на доступ к геолокации"
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            locationListener?.let {
                try { locationManager.removeUpdates(it) } catch (_: SecurityException) {}
            }
        }
    }

    fun onGetLocationClick() {
        locationListener?.let {
            try { locationManager.removeUpdates(it) } catch (_: SecurityException) {}
        }
        locationListener = null

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        ) {
            requestLocation(context, locationManager, { locationText = it }, { locationListener = it })
        } else {
            permissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())
    ) {
        TextButton(onClick = onBack) {
            Text("Назад")
        }
        Text("Второй экран", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = locationText,
            onValueChange = {},
            readOnly = true,
            minLines = 3,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { onGetLocationClick() }, modifier = Modifier.fillMaxWidth()) {
            Text("Определить координаты")
        }
    }
}

@Suppress("DEPRECATION")
fun requestLocation(
    context: Context,
    locationManager: LocationManager,
    onTextChange: (String) -> Unit,
    onListenerChange: (LocationListener?) -> Unit
) {
    onTextChange("Определение координат...")
    val provider = if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) LocationManager.GPS_PROVIDER else LocationManager.NETWORK_PROVIDER

    try {
        val listener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                onTextChange("Широта: ${location.latitude}\nДолгота: ${location.longitude}")
                locationManager.removeUpdates(this)
                onListenerChange(null)
            }
            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {}
        }
        onListenerChange(listener)
        locationManager.requestSingleUpdate(provider, listener, Looper.getMainLooper())
    } catch (_: SecurityException) {
        onTextChange("Пожалуйста, предоставьте разрешение на доступ к геолокации")
    }
}