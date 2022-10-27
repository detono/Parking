package com.tonopuchol.parking.io

import com.tonopuchol.parking.ghent.ParkingData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow

interface ParkingRepository {
    fun getParkingData(scope: CoroutineScope): Flow<Map<String, ParkingData>>
    suspend fun refreshParkingData()
}