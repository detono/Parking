package com.tonopuchol.parking.utils

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

fun Double.round(): Double {
    val value = BigDecimal(this)
    return value.setScale(2, RoundingMode.DOWN).toDouble()
}

fun String.remove(toRemove: String) = this.replace(toRemove, "")

fun String?.isNotNullOrBlank() = !this.isNullOrBlank()

fun Date?.toString(format: String) : String = if (this.isValid()) {
    SimpleDateFormat(format, Locale.getDefault()).format(this!!)
} else ""

fun Date?.isValid() : Boolean {
    if (this == null)
        return false

    val calendar = Calendar.getInstance()
    calendar.time = this
    return (calendar.get(Calendar.YEAR) != 1)
}

fun Context.checkIfPermissionGranted(permission: String): Boolean {
    return (ContextCompat.checkSelfPermission(this, permission)
            == PackageManager.PERMISSION_GRANTED)
}

fun Context.shouldShowPermissionRationale(permission: String): Boolean {

    val activity = this as Activity? ?: return false

    return ActivityCompat.shouldShowRequestPermissionRationale(
        activity,
        permission
    )
}