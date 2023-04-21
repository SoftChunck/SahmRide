package com.simpdev.sahmride.Presentation.Trip

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.graphics.ImageBitmap
import com.mapbox.geojson.Point
import com.simpdev.sahmride.Domain.RideDetails

data class TripUserState(
    val userUid:String? = null,
    val firstName:String? = null,
    val lastName:String? = null,
    val gender:String? = null,
    val duration:String? = null,
    val distance:String? = null,
    val distanceFromDriver:String? = null,
    val durationFromDriver:String? = null,
    val usersAvalible:Boolean = false,
    val loadingUserProfile:Boolean = true,
    val userPic: ImageBitmap? = null,
    val refresh:Int = 1,


    var rideSharingRideDetails: SnapshotStateList<RideDetails> = mutableStateListOf<RideDetails>(),
    var waypoints:MutableList<Point> = emptyList<Point>().toMutableList(),
    var generatedWaypoints:MutableList<Point> = emptyList<Point>().toMutableList(),


    val currentScreen:TripUserScreen = TripUserScreen.TripHome,
    var driverUid:String? = null,
    val driverLng:Double? = null,
    val driverLat:Double? = null,
)
sealed class TripUserScreen{
    object ChatScreen:TripUserScreen()
    object TripHome:TripUserScreen()
    object ReviewScreen:TripUserScreen()
}