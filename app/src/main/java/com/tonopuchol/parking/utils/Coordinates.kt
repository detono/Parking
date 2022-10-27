package com.tonopuchol.parking.utils

import android.location.Location

data class Coordinates(
    val latitude: Double,
    val longitude: Double
) {
    fun calculateDistance(coordinates: Coordinates): Float? {
        val targetLocation = Location("") //provider name is unnecessary

        targetLocation.longitude = coordinates.longitude
        targetLocation.latitude = coordinates.latitude

        val ow = Location("")
        ow.longitude = longitude
        ow.latitude = latitude

        return targetLocation.distanceTo(ow)
    }
}