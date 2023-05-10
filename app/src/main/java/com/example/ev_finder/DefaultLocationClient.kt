package com.example.locationservices

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.os.Looper
import android.util.Log
import com.example.locationservices.ui.theme.hasLocationPermission
import com.google.android.gms.location.*

private const val TAG = "Default Location Client"

class DefaultLocationClient(
    private val context: Context,
    private val client: FusedLocationProviderClient,
) : LocationClient {

    var userLocation: Location? = null

    @SuppressLint("MissingPermission")
    override fun getLocationUpdates(interval: Long) {

        if (!context.hasLocationPermission()) {
            throw LocationClient.LocationException("Missing location permission")
        }

        val locationManager = context.getSystemService(Context.LOCATION_SERVICE)
                as LocationManager
        val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        val isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        if (!isGpsEnabled && !isNetworkEnabled) {
            throw LocationClient.LocationException("GPS is disabled")
        }

        val request = LocationRequest.Builder(
            Priority.PRIORITY_LOW_POWER, interval
        ).apply {
            setMaxUpdates(1)
        }
            .build()

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                super.onLocationResult(result)
                val location = result.lastLocation
                if (location != null) {
                    Log.d(TAG, location.latitude.toString())
                    Log.d(TAG, location.longitude.toString())
                    userLocation = location
                }
            }
        }

        fun requestNewLocationData() {
            client.requestLocationUpdates(
                request,
                locationCallback,
                Looper.getMainLooper()
            )
        }

        client.lastLocation.addOnCompleteListener { task ->
            val location = task.result
            if (location == null) {
                requestNewLocationData()
            } else {
                Log.d(TAG, "Hello")
                Log.d(TAG, location.latitude.toString() + "")
                Log.d(TAG, location.longitude.toString() + "")
                userLocation = location
            }
        }
        client.removeLocationUpdates(locationCallback)
    }

}