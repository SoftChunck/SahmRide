package com.simpdev.sahmride.Presentation.Trip

data class TripUserState(
    val currentScreen:TripUserScreen = TripUserScreen.TripHome,
    val driverUid:String? = null,
    val driverLng:Double? = null,
    val driverLat:Double? = null,
)
sealed class TripUserScreen{
    object ChatScreen:TripUserScreen()
    object TripHome:TripUserScreen()
}