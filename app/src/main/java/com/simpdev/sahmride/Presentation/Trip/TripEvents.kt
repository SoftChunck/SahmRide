package com.simpdev.sahmride.Presentation.Trip

sealed class TripEvents {
    data class userAccepted(val distance:String?): TripEvents()
    object userRejected: TripEvents()
    object chatClicked:TripEvents()
    object tripHomeClicked:TripEvents()
}