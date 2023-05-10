package com.example.locationservices

interface LocationClient {
    fun getLocationUpdates(interval: Long)

     class LocationException(message: String): Exception()

    //TODO flesh out interface
}