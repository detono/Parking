package com.tonopuchol.parking.utils

import android.content.Context
import android.location.Location
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.OnTokenCanceledListener

object LocationUtils{

    private var fusedLocationProviderClient: FusedLocationProviderClient?= null

    // using singleton pattern to get the locationProviderClient
    fun getInstance(appContext: Context): FusedLocationProviderClient{
        if(fusedLocationProviderClient == null)
            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(appContext)
        return fusedLocationProviderClient!!
    }

    fun getCurrentLocation(onSuccess: (Location) -> Unit, onFail: () -> Unit) {
        fusedLocationProviderClient?.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY,
            object : CancellationToken() {
                override fun onCanceledRequested(p0: OnTokenCanceledListener) = CancellationTokenSource().token
                override fun isCancellationRequested() = false
            }
        )
            ?.addOnSuccessListener { onSuccess(it) }
            ?.addOnCanceledListener { onFail() }
            ?.addOnFailureListener { onFail() }
    }

    fun getLastLocation(onSuccess: (Location) -> Unit, onFail: () -> Unit) {
        fusedLocationProviderClient?.lastLocation?.addOnSuccessListener {
            onSuccess(it)
        }?.addOnFailureListener { onFail() }
            ?.addOnFailureListener { onFail() }
    }
}