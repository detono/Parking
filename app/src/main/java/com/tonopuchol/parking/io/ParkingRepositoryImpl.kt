package com.tonopuchol.parking.io

import com.tonopuchol.parking.ghent.ParkingData
import com.tonopuchol.parking.io.network.ParkingCommunicator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ParkingRepositoryImpl @Inject constructor(): ParkingRepository {
    private suspend fun <T : Any> call(function: suspend () -> T?): T? {
        return try {
            function()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun getCalls() = ParkingCommunicator.getCalls()

    override fun getParkingData(scope: CoroutineScope): Flow<Map<String, ParkingData>> {
        val flow = MutableStateFlow<Map<String, ParkingData>>(emptyMap())

        scope.launch (Dispatchers.IO) {
            val result = call {
                getCalls().getGhent()
            }

            result?.let { r ->
                withContext(Dispatchers.Main) {
                    flow.emit(r.associateBy({ it.id ?: "" }, { it }))
                }
            }
        }

        return flow
    }


    override suspend fun refreshParkingData() {
        //REFRESH ONLINE
    }
}