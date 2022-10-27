package com.tonopuchol.parking.ghent

import com.tonopuchol.parking.utils.Coordinates

data class ParkingLocationDimension(
    val specificAccessInformation: List<String>,
    val level: String?,
    val roadNumber: String?,
    val roadName: String?,
    val contactDetailsTelephoneNumber: String?,
    val coordinatesForDisplay: Coordinates?
) {
    companion object {
        fun getFilledForTest() = ParkingLocationDimension(
            specificAccessInformation = listOf("inrit"),
            level = "0",
            roadName = "?",
            roadNumber = "?",
            coordinatesForDisplay = Coordinates(latitude = 51.07256988, longitude = 3.714831357),
            contactDetailsTelephoneNumber = "Tel.: 09 266 29 00\\n(permanentie)\\Tel.: 099 266 29 01\\n(tijdens kantooruren)"
        )
    }
}