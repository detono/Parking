package com.tonopuchol.parking.ghent

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import java.util.*

data class ParkingData(
    val name: String?,
    @SerializedName("lastupdate")
    val lastUpdate: Date?,
    @SerializedName("totalcapacity")
    val totalCapacity: Int?,
    @SerializedName("availablecapacity")
    val availableCapacity: Int?,
    val occupation: Int?,
    val type: String?,
    val description: String?,
    @SerializedName("urllinkaddress")
    val url: String?,
    val id: String?,
    @SerializedName("openingtimesdescription")
    val openingTimes: String?,
    @SerializedName("isopennow")
    val open: Int?,
    @SerializedName("temporaryclosed")
    val temporaryClosed: Int?,
    @SerializedName("operatorinformation")
    val operator: String?,
    @SerializedName("freeparking")
    val freeParking: Int?,
    @SerializedName("occupancytrend")
    val occupancyTrend: String?,
    @SerializedName("location")
    val location: ParkingLocation,
    @SerializedName("categorie")
    val category: String?,
    @SerializedName("text")
    val remark: String?,
    @SerializedName("locationanddimension")
    val locationInformation: String?
) {
    companion object {
        fun getFilledForTest() = ParkingData(
                name = "Getouw",
                lastUpdate = Date(),
                totalCapacity = 350,
                availableCapacity = 340,
                occupation = 10,
                type = "offStreetParkingGround",
                description = "Bovengrondse parkeergarage Het Getouw in Gent",
                id = "https://stad.gent/nl/mobiliteit-openbare-werken/parkeren/parkings-gent/parking-het-getouw",
                openingTimes = "24/7",
                open = 1,
                temporaryClosed = 0,
                operator = "Mobiliteitsbedrijf Gent",
                freeParking = 0,
                url = "https://stad.gent/nl/mobiliteit-openbare-werken/parkeren/parkings-gent/parking-het-getouw",
                occupancyTrend = "unknown",
                locationInformation = Gson().toJson(ParkingLocationDimension.getFilledForTest()),
                location = ParkingLocation(lon = 3.714831357, lat = 51.07256988),
                category = "parking in LEZ",
                remark = null
            )

    }

    var distance: Double? = null

    fun getLocationInfo() = Gson().fromJson(locationInformation, ParkingLocationDimension::class.java)
}