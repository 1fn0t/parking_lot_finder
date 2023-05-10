package com.example.composeweatherapp.weather.api

import com.example.ev_finder.response_classes.ParkingResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path


interface LocatorApi {

    @GET("/location/{lat1}/{lat2}/{lon1}/{lon2}/{density}")
    suspend fun fetchStations(
        @Path("lat1") lat1: Double,
        @Path("lat2") lat2: Double,
        @Path("lon1") lon1: Double,
        @Path("lon2") lon2: Double,
        @Path("density") density: Int,
        @Header("X-RapidAPI-Host") rapidApiHost: String,
        @Header("X-RapidAPI-Key") rapidApiKey: String
    ): Response<ParkingResponse>

}