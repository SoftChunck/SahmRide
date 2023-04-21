package com.simpdev.sahmride.Presentation.Trip

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.graphics.ImageBitmap
import com.mapbox.geojson.Point
import com.simpdev.sahmride.Domain.RideDetails

data class TripState(

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

    var waypoints:MutableList<Point> = emptyList<Point>().toMutableList(),
    var rideSharingRideDetails: SnapshotStateList<RideDetails> = mutableStateListOf<RideDetails>(),

    val currentScreen:TripScreen = TripScreen.TripHome
)
sealed class TripScreen{
    object ChatScreen:TripScreen()
    object TripHome:TripScreen()
}