package com.simpdev.sahmride.Presentation.Trip

data class TripState(
    val currentScreen:TripScreen = TripScreen.TripHome
)
sealed class TripScreen{
    object ChatScreen:TripScreen()
    object TripHome:TripScreen()
}