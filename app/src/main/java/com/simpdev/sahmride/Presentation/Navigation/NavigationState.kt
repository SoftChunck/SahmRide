package com.simpdev.sahmride.Presentation.Navigation

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.graphics.ImageBitmap
import com.mapbox.geojson.Point
import com.simpdev.sahmride.Domain.RideDetails

data class NavigationState(
    val loadedProfilePic:Boolean = false,
    val loadedUserData:Boolean = false,
    val userAccepted:Boolean = false,
    //Ride details

    val usersAvalible:Boolean = false,
    val loadingUserProfile:Boolean = true,

    val userUid:String? = null,
    val firstName:String? = null,
    val lastName:String? = null,
    val gender:String? = null,
    val vehicleNumber:String? = null,
    val duration:String? = null,
    val distance:String? = null,
    val review:String? = null,
    val rating:Int? = null,
    val pickup: Point? = null,
    val destination: Point? = null,
    val userPic: ImageBitmap? = null,
    val distanceFromDriver:String? = null,
    val durationFromDriver:String? = null,
    val rideStatus:String? =  null,

    val currentScreen: NavigationScreen = NavigationScreen.LoadingScreen,

    var rideSharingUsers:MutableList<userInfo> = emptyList<userInfo>().toMutableList(),
//    var rideSharingRideDetails:MutableList<RideDetails> = emptyList<RideDetails>().toMutableList(),

    var rideSharingRideDetails: SnapshotStateList<RideDetails> = mutableStateListOf<RideDetails>(),


    var waypoints:MutableList<Point> = emptyList<Point>().toMutableList()

)

data class userInfo(
    var userUid:String? = null,
    var firstName:String? = null,
    var lastName:String? = null,
    var gender:String? = null,
    var userPic: ImageBitmap? = null,
)

sealed class NavigationScreen(){
    object WaitForRequest:NavigationScreen()
    object LoadingScreen:NavigationScreen()
    object UserAcceptedScreen:NavigationScreen()
    object RideAcceptedScreen:NavigationScreen()
    object AppMainScreen:NavigationScreen()

}