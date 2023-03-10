package com.simpdev.sahmride.Presentation.Ride

import androidx.compose.ui.graphics.ImageBitmap
import com.simpdev.sahmride.Domain.Data.AvalibleDriverData

data class RideState(
    val currentRideScreen: RideScreen = RideScreen.RideHome,
    val userUid:String? = null,
    val firstName:String? = null,
    val lastName:String? = null,
    val gender:String? = null,
    val userPic: ImageBitmap? = null,
    val selectedDriverData:AvalibleDriverData? = null,
)

sealed class RideScreen{
    object RideHome:RideScreen()
    object TripScreen:RideScreen()
    object DriverProfileScreen:RideScreen()
}
