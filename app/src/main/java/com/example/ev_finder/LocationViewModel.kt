package com.example.locationservices

import android.annotation.SuppressLint
import android.app.Application
import android.location.Location
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

private const val TAG = "Location ViewModel"

class LocationViewModel(
    private val client: DefaultLocationClient
): ViewModel() {
    var userLocation: Location? by mutableStateOf(null)
        private set

    @SuppressLint("MissingPermission")
    fun getUserLocation(){
        try {
            client.getLocationUpdates(5)
            Log.d(TAG, client.userLocation.toString())
            userLocation = client.userLocation
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

class LocationViewModelFactory(private val locationClient: DefaultLocationClient) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return LocationViewModel(locationClient) as T
    }
}