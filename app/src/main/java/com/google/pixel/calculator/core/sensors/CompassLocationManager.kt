package com.google.pixel.calculator.core.sensors

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.abs

data class CompassLocationData(
    val latitude: Double = 40.093056,
    val longitude: Double = 120.033611,
    val altitude: Double = 991.0,
    val latitudeDms: String = "40°5'35\"",
    val latitudeCardinal: String = "NL",
    val longitudeDms: String = "120°2'1\"",
    val longitudeCardinal: String = "EL",
    val altitudeFormatted: String = "991m",
    val locationName: String = "Florida",
    val hasPermission: Boolean = false,
    val isGpsActive: Boolean = false
)

class CompassLocationManager(private val context: Context) {

    private val locationManager = context.applicationContext.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
    private val scope = CoroutineScope(Dispatchers.Main + Job())

    private val _locationFlow = MutableStateFlow(
        CompassLocationData(
            latitude = 40.093056,
            longitude = 120.033611,
            altitude = 991.0,
            latitudeDms = "40°5'35\"",
            latitudeCardinal = "NL",
            longitudeDms = "120°2'1\"",
            longitudeCardinal = "EL",
            altitudeFormatted = "991m",
            locationName = "Posizione GPS",
            hasPermission = hasPermission()
        )
    )
    val locationFlow: StateFlow<CompassLocationData> = _locationFlow.asStateFlow()

    private var locationListener: LocationListener? = null

    fun hasPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("MissingPermission")
    fun startListening() {
        val permitted = hasPermission()
        val mgr = locationManager
        if (!permitted || mgr == null) {
            _locationFlow.value = _locationFlow.value.copy(hasPermission = false)
            return
        }

        // Recupera subito l'ultima posizione nota per azzerare i tempi di caricamento
        val lastGps = runCatching { mgr.getLastKnownLocation(LocationManager.GPS_PROVIDER) }.getOrNull()
        val lastNetwork = runCatching { mgr.getLastKnownLocation(LocationManager.NETWORK_PROVIDER) }.getOrNull()
        val bestLast = lastGps ?: lastNetwork

        if (bestLast != null) {
            updateFromLocation(bestLast, permitted)
        }

        val listener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                updateFromLocation(location, true)
            }
            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {}
            @Deprecated("Deprecated in Java")
            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
        }
        locationListener = listener

        try {
            if (mgr.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                mgr.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    3000L,
                    2f,
                    listener
                )
            }
            if (mgr.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                mgr.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    5000L,
                    5f,
                    listener
                )
            }
        } catch (_: SecurityException) {
            _locationFlow.value = _locationFlow.value.copy(hasPermission = false)
        } catch (_: Exception) {
            // Ignora se provider non disponibili
        }
    }

    fun stopListening() {
        locationListener?.let { listener ->
            try {
                locationManager?.removeUpdates(listener)
            } catch (_: Exception) {}
            locationListener = null
        }
    }

    private fun updateFromLocation(location: Location, permitted: Boolean) {
        val lat = location.latitude
        val lon = location.longitude
        val alt = if (location.hasAltitude()) location.altitude else _locationFlow.value.altitude

        val (latDms, latCard) = formatCoordinate(lat, isLatitude = true)
        val (lonDms, lonCard) = formatCoordinate(lon, isLatitude = false)
        val altFmt = "${alt.toInt()}m"

        _locationFlow.value = _locationFlow.value.copy(
            latitude = lat,
            longitude = lon,
            altitude = alt,
            latitudeDms = latDms,
            latitudeCardinal = latCard,
            longitudeDms = lonDms,
            longitudeCardinal = lonCard,
            altitudeFormatted = altFmt,
            hasPermission = permitted,
            isGpsActive = true
        )

        // Risoluzione inversa nome città / località tramite Geocoder asincrono
        fetchGeocodedName(lat, lon)
    }

    private fun fetchGeocodedName(lat: Double, lon: Double) {
        scope.launch(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(context, Locale.getDefault())
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    geocoder.getFromLocation(lat, lon, 1) { addresses ->
                        val address = addresses.firstOrNull()
                        val cityName = address?.locality
                            ?: address?.subAdminArea
                            ?: address?.adminArea
                            ?: address?.countryName
                        if (!cityName.isNullOrBlank()) {
                            _locationFlow.value = _locationFlow.value.copy(locationName = cityName)
                        }
                    }
                } else {
                    @Suppress("DEPRECATION")
                    val addresses = geocoder.getFromLocation(lat, lon, 1)
                    val address = addresses?.firstOrNull()
                    val cityName = address?.locality
                        ?: address?.subAdminArea
                        ?: address?.adminArea
                        ?: address?.countryName
                    if (!cityName.isNullOrBlank()) {
                        _locationFlow.value = _locationFlow.value.copy(locationName = cityName)
                    }
                }
            } catch (_: Exception) {
                // Silently fallback se geocoder offline
            }
        }
    }

    companion object {
        fun formatCoordinate(decimal: Double, isLatitude: Boolean): Pair<String, String> {
            val isPositive = decimal >= 0
            val absVal = abs(decimal)
            val totalSeconds = kotlin.math.round(absVal * 3600.0).toLong()
            val degrees = (totalSeconds / 3600).toInt()
            val remainder = totalSeconds % 3600
            val minutes = (remainder / 60).toInt()
            val seconds = (remainder % 60).toInt()

            val cardinal = if (isLatitude) {
                if (isPositive) "NL" else "SL"
            } else {
                if (isPositive) "EL" else "WL"
            }

            return Pair("${degrees}°${minutes}'${seconds}\"", cardinal)
        }
    }
}
