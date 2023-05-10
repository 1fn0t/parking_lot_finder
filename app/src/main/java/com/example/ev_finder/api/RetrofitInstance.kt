package com.example.composeweatherapp.weather.api

import android.util.Log
import com.example.ev_finder.response_classes.ParkingResponse
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException

private const val TAG = "API"
private const val key = "dfa69bd833msh15503a8e1f0d563p1da729jsn69883c3a1d23"
private const val host = "next-parking-lot.p.rapidapi.com"
private const val density = 13
private const val delta = 0.025

object RetrofitInstance {

    val api: LocatorApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://next-parking-lot.p.rapidapi.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(LocatorApi::class.java)
    }
}

suspend fun fetchStations(lat: Double, lon: Double): ParkingResponse? {
    try {
        val minLat = lat - delta
        val maxLat = lat + delta
        val minLng = lon - delta
        val maxLng = lon + delta
        val response = RetrofitInstance.api.fetchStations(
            lat1 = minLat, lat2 = maxLat, lon1 = minLng, lon2 = maxLng, density = density, rapidApiKey = key, rapidApiHost = host)
        if (response.isSuccessful) {
            Log.d(TAG, "Result of current day ${response.body()}")
            return response.body()
        } else {
            Log.d(TAG, "Not successful ${response}")
        }
    } catch(e: IOException) {
        Log.e(TAG, "IOException")
    } catch(e: HttpException) {
        Log.e(TAG, "Request Error")
    }
    return null
}