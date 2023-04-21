package com.simpdev.sahmride.Presentation.Trip

sealed class TripUserEvents {
    data class userAccepted(val distance:String?): TripUserEvents()
    object userRejected: TripUserEvents()
    object chatClicked:TripUserEvents()
    object tripHomeClicked:TripUserEvents()
}