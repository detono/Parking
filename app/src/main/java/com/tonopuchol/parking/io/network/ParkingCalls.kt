package com.tonopuchol.parking.io.network

import com.tonopuchol.parking.ghent.ParkingData
import retrofit2.http.GET

interface ParkingCalls {
    @GET("v2/catalog/datasets/bezetting-parkeergarages-real-time/exports/json")
    suspend fun getGhent(): List<ParkingData>
}